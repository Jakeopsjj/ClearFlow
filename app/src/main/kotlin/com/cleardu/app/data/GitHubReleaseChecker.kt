package com.cleardu.app.data

import android.util.Log
import com.cleardu.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * GitHub Release 版本检查器。
 *
 * 通过 GitHub API 获取仓库 Release 列表，支持语义化版本比较和
 * Debug / Release 双通道更新检查。
 *
 * 每个版本只创建一个 Release（tag 如 v1.12.1），同时包含 Debug 和 Release 两个 APK 资产。
 * - Debug 通道：匹配资产名包含 "-debug.apk" 的 APK
 * - Release 通道：匹配资产名包含 "-release.apk" 的 APK
 * - 版本比较：使用语义化版本（major.minor.patch）比较，仅当远端版本严格高于当前版本时提示更新
 *
 * 本模块使用独立 OkHttp 请求链路，不服用 Pexels 代理接口。
 */
object GitHubReleaseChecker {

    private const val TAG = "GitHubReleaseChecker"
    private const val GITHUB_API_URL = "https://api.github.com/repos/Jakeopsjj/ClearFlow/releases"
    private const val TIMEOUT_MS = 10_000L
    private const val PER_PAGE = 30
    private const val MAX_RETRIES = 2

    /**
     * 更新检查异常类型，用于 UI 层分类展示提示文案。
     */
    sealed class CheckUpdateException(message: String, cause: Throwable? = null) :
        Exception(message, cause) {
        /** IO 网络异常（DNS解析失败、连接超时、无网络等） */
        class NetworkException(message: String, cause: Throwable? = null) :
            CheckUpdateException(message, cause)

        /** HTTP 非 200 响应 */
        class HttpException(statusCode: Int, message: String) :
            CheckUpdateException("HTTP $statusCode: $message")

        /** JSON 解析异常 */
        class ParseException(message: String, cause: Throwable? = null) :
            CheckUpdateException(message, cause)
    }

    /**
     * 更新检查结果封装。
     */
    sealed class CheckUpdateResult {
        data class Success(val release: ReleaseInfo) : CheckUpdateResult()
        data class Error(val exception: CheckUpdateException) : CheckUpdateResult()
    }

    // 独立 OkHttpClient，不服用 Pexels 代理
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .build()
    }

    /**
     * 获取最新 Release 信息（匹配当前构建类型通道）。
     *
     * @param isDebug 当前是否为 Debug 构建
     * @return 匹配通道的最新 [ReleaseInfo]，或 null
     */
    suspend fun fetchLatestRelease(isDebug: Boolean): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val releases = fetchReleases()
            if (releases.isEmpty()) return@withContext null

            // 取版本号最高的 Release
            val latest = releases.maxByOrNull { release ->
                val parts = release.parsedVersion
                (parts.getOrElse(0) { 0 }) * 1_000_000L +
                    (parts.getOrElse(1) { 0 }) * 1_000L +
                    (parts.getOrElse(2) { 0 })
            } ?: return@withContext null

            // 按构建类型匹配对应的 APK 资产
            val apkSuffix = if (isDebug) "-debug.apk" else "-release.apk"
            val apkAsset = latest.assets.firstOrNull { it.name.endsWith(apkSuffix) }

            if (apkAsset == null) {
                Log.w(TAG, "No ${if (isDebug) "debug" else "release"} APK in release ${latest.tagName}")
                return@withContext null
            }

            latest.copy(apkUrl = apkAsset.url)
        } catch (e: CheckUpdateException) {
            Log.e(TAG, "Failed to fetch latest release: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            null
        }
    }

    /**
     * 获取所有 Release（兼容旧接口，用于更新日志弹窗等场景）。
     */
    suspend fun fetchLatestRelease(): ReleaseInfo? = fetchLatestRelease(isDebug = false)

    /**
     * 按 tag 名称获取指定 Release，用于获取当前版本对应的更新日志。
     *
     * @param tagName GitHub Release 的 tag 名称，如 "v1.12.1" 或 "v1.12.1-debug"
     * @return 匹配的 [ReleaseInfo]，或 null
     */
    suspend fun fetchReleaseByTag(tagName: String): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val releases = fetchReleases()
            releases.firstOrNull { it.tagName == tagName }
        } catch (e: CheckUpdateException) {
            Log.e(TAG, "Failed to fetch release by tag: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            null
        }
    }

    /**
     * 带结果分类的更新检查，供 UI 层使用。
     * 返回 [CheckUpdateResult] 以便 UI 层根据异常类型展示不同提示文案。
     */
    suspend fun checkUpdate(isDebug: Boolean): CheckUpdateResult = withContext(Dispatchers.IO) {
        try {
            val releases = fetchReleases()
            if (releases.isEmpty()) {
                return@withContext CheckUpdateResult.Error(
                    CheckUpdateException.ParseException("未获取到 Release 列表")
                )
            }
            val latest = releases.maxByOrNull { release ->
                val parts = release.parsedVersion
                (parts.getOrElse(0) { 0 }) * 1_000_000L +
                    (parts.getOrElse(1) { 0 }) * 1_000L +
                    (parts.getOrElse(2) { 0 })
            } ?: return@withContext CheckUpdateResult.Error(
                CheckUpdateException.ParseException("无法解析版本信息")
            )
            CheckUpdateResult.Success(latest)
        } catch (e: CheckUpdateException) {
            CheckUpdateResult.Error(e)
        } catch (e: Exception) {
            CheckUpdateResult.Error(
                CheckUpdateException.NetworkException("未知错误: ${e.message}", e)
            )
        }
    }

    /**
     * 比较两个语义化版本号。
     *
     * @return 正数：v1 > v2；负数：v1 < v2；0：相等
     */
    fun compareVersion(v1: String, v2: String): Int {
        val parts1 = parseVersion(v1)
        val parts2 = parseVersion(v2)

        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val a = parts1.getOrElse(i) { 0 }
            val b = parts2.getOrElse(i) { 0 }
            if (a != b) return a - b
        }
        return 0
    }

    /**
     * 判断 [githubVersion] 是否严格高于 [currentVersion]。
     */
    fun isNewerVersion(githubVersion: String, currentVersion: String): Boolean {
        return compareVersion(githubVersion, currentVersion) > 0
    }

    /**
     * 获取用户友好的错误提示文案。
     */
    fun getUserFriendlyMessage(exception: CheckUpdateException): String = when (exception) {
        is CheckUpdateException.NetworkException -> "网络无法连接，请检查网络后重试"
        is CheckUpdateException.HttpException -> "服务器响应异常（${exception.message}），请稍后重试"
        is CheckUpdateException.ParseException -> "数据解析失败，请稍后重试"
    }

    // ==================== 内部实现 ====================

    /**
     * 通过独立 OkHttp 请求获取 Release 列表，支持最多 [MAX_RETRIES] 次重试。
     * 优先使用代理服务器，失败后回退直连 GitHub API。
     */
    private fun fetchReleases(): List<ReleaseInfo> {
        val proxyUrl = BuildConfig.GITHUB_PROXY_URL.trimEnd('/')
        val proxyFullUrl = "$proxyUrl/api/github/releases?owner=Jakeopsjj&repo=ClearFlow&per_page=$PER_PAGE"
        val directUrl = "$GITHUB_API_URL?per_page=$PER_PAGE"

        // 先尝试代理，失败后回退直连
        var lastException: Exception? = null

        // 尝试代理
        for (attempt in 1..MAX_RETRIES) {
            try {
                val body = fetchWithOkHttp(proxyFullUrl)
                return parseReleases(body)
            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "Proxy attempt $attempt failed: ${e.message}")
            } catch (e: CheckUpdateException) {
                // 非网络错误不重试
                throw e
            }
        }

        Log.w(TAG, "All proxy attempts failed, trying direct GitHub API")

        // 回退直连 GitHub API
        for (attempt in 1..MAX_RETRIES) {
            try {
                val body = fetchWithOkHttp(directUrl)
                return parseReleases(body)
            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "Direct API attempt $attempt failed: ${e.message}")
            } catch (e: CheckUpdateException) {
                throw e
            }
        }

        throw CheckUpdateException.NetworkException(
            "网络无法连接，已重试 ${MAX_RETRIES * 2} 次",
            lastException
        )
    }

    /**
     * 使用 OkHttp 执行 GET 请求，返回响应 body 字符串。
     */
    private fun fetchWithOkHttp(urlString: String): String {
        val request = Request.Builder()
            .url(urlString)
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "ClearFlow-App")
            .get()
            .build()

        val response = client.newCall(request).execute()
        val responseCode = response.code

        if (responseCode != 200) {
            response.close()
            throw CheckUpdateException.HttpException(
                responseCode,
                "HTTP $responseCode"
            )
        }

        val body = response.body?.string() ?: run {
            response.close()
            throw CheckUpdateException.ParseException("响应 body 为空")
        }
        response.close()
        return body
    }

    /**
     * 解析 JSON 响应为 ReleaseInfo 列表。
     */
    private fun parseReleases(body: String): List<ReleaseInfo> {
        return try {
            val jsonArray = JSONArray(body)
            (0 until jsonArray.length()).map { i ->
                val json = jsonArray.getJSONObject(i)
                val assets = json.optJSONArray("assets")?.let { assetsArray ->
                    (0 until assetsArray.length()).map { j ->
                        val asset = assetsArray.getJSONObject(j)
                        ReleaseAsset(
                            name = asset.optString("name", ""),
                            url = asset.optString("browser_download_url", ""),
                            size = asset.optLong("size", 0)
                        )
                    }
                } ?: emptyList()

                ReleaseInfo(
                    tagName = json.optString("tag_name", ""),
                    versionName = json.optString("tag_name", "").removePrefix("v"),
                    body = json.optString("body", ""),
                    publishedAt = json.optString("published_at", ""),
                    htmlUrl = json.optString("html_url", ""),
                    apkUrl = assets.firstOrNull { it.name.endsWith(".apk") }?.url ?: "",
                    assets = assets
                )
            }
        } catch (e: org.json.JSONException) {
            throw CheckUpdateException.ParseException("JSON 解析失败: ${e.message}", e)
        }
    }

    /**
     * 解析版本号字符串为整数列表。
     * "1.11.0" -> [1, 11, 0]
     * "1.11.0-debug" -> [1, 11, 0]  (忽略后缀)
     */
    private fun parseVersion(version: String): List<Int> {
        val clean = version
            .removePrefix("v")
            .substringBefore("-")
            .trim()
        return clean.split(".").mapNotNull { it.toIntOrNull() }
    }

    /**
     * ReleaseInfo 的解析后版本号，用于排序比较。
     */
    private val ReleaseInfo.parsedVersion: List<Int>
        get() = parseVersion(versionName.ifBlank { tagName.removePrefix("v") })
}

/**
 * GitHub Release 资产信息。
 */
data class ReleaseAsset(
    val name: String,
    val url: String,
    val size: Long
)

/**
 * GitHub Release 信息。
 */
data class ReleaseInfo(
    val tagName: String,
    val versionName: String,
    val body: String,
    val publishedAt: String,
    val htmlUrl: String,
    val apkUrl: String,
    val assets: List<ReleaseAsset> = emptyList()
)