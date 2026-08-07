package com.cleardu.app.data

import android.util.Log
import com.cleardu.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
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
 * ## 需求3：异常分类处理与重试机制
 *
 * 本模块使用独立的 OkHttp 客户端，不复用 pexels 代理接口。
 * - 仅 IOException IO 网络异常提示"网络无法连接"
 * - HTTP 错误码使用独立提示文案
 * - JSON 解析异常使用独立提示文案
 * - 单次请求最多重试 2 次，打印原始异常调试日志
 */
object GitHubReleaseChecker {

    private const val TAG = "GitHubReleaseChecker"
    private const val GITHUB_API_URL = "https://api.github.com/repos/Jakeopsjj/ClearFlow/releases"
    private const val TIMEOUT_MS = 10_000
    private const val PER_PAGE = 30

    /** [修改点-需求3] 更新请求最大重试次数（首次 + 2 次重试 = 共 3 次尝试）。 */
    private const val MAX_RETRY = 2

    /** [修改点-需求3] 独立 OkHttp 客户端，不复用 pexels 代理接口。 */
    private val updateHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * [修改点-需求3] 更新检查结果分类。
     *
     * 用于区分不同失败原因，向用户展示精确的错误提示文案，
     * 而不是把所有失败统一报"网络失败"。
     */
    sealed class UpdateFetchResult {
        /** 成功获取到 Release 信息。 */
        data class Success(val release: ReleaseInfo) : UpdateFetchResult()

        /** 仅 IOException IO 网络异常 — 提示"网络无法连接"。 */
        data object NetworkUnreachable : UpdateFetchResult()

        /** HTTP 错误码（如 404/500/503） — 提示服务器异常。 */
        data class HttpError(val code: Int) : UpdateFetchResult()

        /** JSON 解析异常 — 提示数据解析失败。 */
        data object ParseError : UpdateFetchResult()

        /** 其它未知异常 — 提示检查失败。 */
        data object UnknownError : UpdateFetchResult()
    }

    /**
     * [修改点-需求3] 获取最新 Release 信息（匹配当前构建类型通道）。
     *
     * 返回分类后的 [UpdateFetchResult]，调用方可据此展示精确的错误提示。
     *
     * @param isDebug 当前是否为 Debug 构建
     * @return 分类结果，[UpdateFetchResult.Success] 携带匹配通道的最新 [ReleaseInfo]
     */
    suspend fun fetchLatestReleaseResult(isDebug: Boolean): UpdateFetchResult =
        withContext(Dispatchers.IO) {
            var lastError: UpdateFetchResult = UpdateFetchResult.UnknownError

            // 重试循环：首次 + MAX_RETRY 次重试
            for (attempt in 0..MAX_RETRY) {
                if (attempt > 0) {
                    Log.d(TAG, "重试第 $attempt/$MAX_RETRY 次")
                    // 重试前短暂等待，避免立即重打导致雪崩
                    try {
                        Thread.sleep(500L * attempt)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return@withContext lastError
                    }
                }

                when (val result = fetchReleasesWithClassification()) {
                    is InternalResult.Success -> {
                        val releases = result.releases
                        if (releases.isEmpty()) {
                            Log.w(TAG, "Release 列表为空")
                            lastError = UpdateFetchResult.ParseError
                            continue
                        }

                        // 取版本号最高的 Release
                        val latest = releases.maxByOrNull { release ->
                            val parts = release.parsedVersion
                            (parts.getOrElse(0) { 0 }) * 1_000_000L +
                                (parts.getOrElse(1) { 0 }) * 1_000L +
                                (parts.getOrElse(2) { 0 })
                        }

                        if (latest == null) {
                            lastError = UpdateFetchResult.ParseError
                            continue
                        }

                        // 按构建类型匹配对应的 APK 资产
                        val apkSuffix = if (isDebug) "-debug.apk" else "-release.apk"
                        val apkAsset = latest.assets.firstOrNull { it.name.endsWith(apkSuffix) }

                        if (apkAsset == null) {
                            Log.w(TAG, "No ${if (isDebug) "debug" else "release"} APK in release ${latest.tagName}")
                            lastError = UpdateFetchResult.ParseError
                            continue
                        }

                        return@withContext UpdateFetchResult.Success(
                            latest.copy(apkUrl = apkAsset.url)
                        )
                    }
                    is InternalResult.NetworkUnreachable -> {
                        // IO 异常才重试
                        lastError = UpdateFetchResult.NetworkUnreachable
                        Log.d(TAG, "IO 异常，准备重试")
                    }
                    is InternalResult.HttpError -> {
                        // HTTP 4xx 不重试（除了 429），5xx 重试
                        lastError = UpdateFetchResult.HttpError(result.code)
                        if (result.code in 400..499 && result.code != 429) {
                            Log.d(TAG, "HTTP ${result.code} 客户端错误，不重试")
                            return@withContext UpdateFetchResult.HttpError(result.code)
                        }
                        Log.d(TAG, "HTTP ${result.code}，准备重试")
                    }
                    is InternalResult.ParseError -> {
                        // JSON 解析异常不重试
                        return@withContext UpdateFetchResult.ParseError
                    }
                    is InternalResult.UnknownError -> {
                        lastError = UpdateFetchResult.UnknownError
                        Log.d(TAG, "未知异常，准备重试")
                    }
                }
            }

            lastError
        }

    /**
     * 兼容旧接口：返回 [ReleaseInfo] 或 null。
     * 新代码应优先使用 [fetchLatestReleaseResult] 获取分类错误。
     */
    suspend fun fetchLatestRelease(isDebug: Boolean): ReleaseInfo? {
        return when (val result = fetchLatestReleaseResult(isDebug)) {
            is UpdateFetchResult.Success -> result.release
            else -> {
                Log.e(TAG, "fetchLatestRelease 失败: $result")
                null
            }
        }
    }

    /**
     * 兼容旧接口：获取所有 Release（用于更新日志弹窗等场景）。
     */
    suspend fun fetchLatestRelease(): ReleaseInfo? = fetchLatestRelease(isDebug = false)

    /**
     * 按 tag 名称获取指定 Release，用于获取当前版本对应的更新日志。
     *
     * @param tagName GitHub Release 的 tag 名称，如 "v1.12.1" 或 "v1.12.1-debug"
     * @return 匹配的 [ReleaseInfo]，或 null
     */
    suspend fun fetchReleaseByTag(tagName: String): ReleaseInfo? = withContext(Dispatchers.IO) {
        when (val result = fetchReleasesWithClassification()) {
            is InternalResult.Success -> result.releases.firstOrNull { it.tagName == tagName }
            else -> {
                Log.e(TAG, "fetchReleaseByTag 失败: $result")
                null
            }
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

    // ==================== 内部实现 ====================

    /**
     * [修改点-需求3] 内部请求结果，携带 Release 列表或分类错误。
     */
    private sealed class InternalResult {
        data class Success(val releases: List<ReleaseInfo>) : InternalResult()
        data object NetworkUnreachable : InternalResult()
        data class HttpError(val code: Int) : InternalResult()
        data object ParseError : InternalResult()
        data object UnknownError : InternalResult()
    }

    /**
     * [修改点-需求3] 请求 GitHub API 并分类异常。
     *
     * - 直连 GitHub API，不复用 pexels 代理接口
     * - IOException → NetworkUnreachable
     * - HTTP 非 200 → HttpError
     * - JSON 解析异常 → ParseError
     */
    private suspend fun fetchReleasesWithClassification(): InternalResult =
        withContext(Dispatchers.IO) {
            // 直连 GitHub API（需求3：禁止复用 pexels 代理接口）
            val requestUrl = "$GITHUB_API_URL?per_page=$PER_PAGE"
            val request = Request.Builder()
                .url(requestUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "ClearFlow-App")
                .build()

            val responseBody: String
            try {
                updateHttpClient.newCall(request).execute().use { response ->
                    val code = response.code
                    if (code != HttpURLConnection.HTTP_OK) {
                        // 打印原始调试日志
                        Log.w(TAG, "HTTP 错误码: $code")
                        return@withContext InternalResult.HttpError(code)
                    }
                    responseBody = response.body?.string().orEmpty()
                    if (responseBody.isBlank()) {
                        Log.w(TAG, "响应 body 为空")
                        return@withContext InternalResult.ParseError
                    }
                }
            } catch (e: IOException) {
                // 仅 IOException IO 网络异常 → "网络无法连接"
                Log.e(TAG, "IO 网络异常: ${e.message}", e)
                return@withContext InternalResult.NetworkUnreachable
            } catch (e: Exception) {
                Log.e(TAG, "未知异常: ${e.message}", e)
                return@withContext InternalResult.UnknownError
            }

            // JSON 解析
            try {
                val jsonArray = JSONArray(responseBody)
                val releases = (0 until jsonArray.length()).map { i ->
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
                InternalResult.Success(releases)
            } catch (e: JSONException) {
                Log.e(TAG, "JSON 解析异常: ${e.message}", e)
                InternalResult.ParseError
            } catch (e: Exception) {
                Log.e(TAG, "解析未知异常: ${e.message}", e)
                InternalResult.UnknownError
            }
        }

    /**
     * 解析版本号字符串为整数列表。
     * "1.11.0" -> [1, 11, 0]
     * "1.11.0-debug" -> [1, 11, 0]  (忽略后缀)
     */
    private fun parseVersion(version: String): List<Int> {
        // 去除前导 "v" 和后缀（如 "-debug"）
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
    val name: String,   // 文件名，如 "cleardu-v1.12.1-release.apk"
    val url: String,    // 下载 URL
    val size: Long      // 文件大小（字节）
)

/**
 * GitHub Release 信息。
 */
data class ReleaseInfo(
    val tagName: String,       // e.g. "v1.10.0"
    val versionName: String,   // e.g. "1.10.0"
    val body: String,          // release notes (markdown)
    val publishedAt: String,   // ISO 8601 timestamp
    val htmlUrl: String,       // GitHub release page URL
    val apkUrl: String,        // APK download URL（当前通道匹配的）
    val assets: List<ReleaseAsset> = emptyList()  // 所有资产列表
)
