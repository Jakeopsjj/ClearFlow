package com.cleardu.app.data

import android.util.Log
import com.cleardu.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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
 */
object GitHubReleaseChecker {

    private const val TAG = "GitHubReleaseChecker"
    private const val GITHUB_API_URL = "https://api.github.com/repos/Jakeopsjj/ClearFlow/releases"
    private const val TIMEOUT_MS = 10_000
    private const val PER_PAGE = 30

    // 代理服务器 URL（解决国内无法直接访问 GitHub API 的问题）
    private val PROXY_URL: String by lazy {
        BuildConfig.GITHUB_PROXY_URL.trimEnd('/')
    }

    /**
     * 获取最新 Release 信息（匹配当前构建类型通道）。
     *
     * 不再按 tag 名称区分通道，而是获取最新 Release 后按资产名匹配对应的 APK。
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch latest release: ${e.message}", e)
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch release by tag: ${e.message}", e)
            null
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

    private fun fetchReleases(): List<ReleaseInfo> {
        // 先尝试代理服务器，再回退到直连 GitHub API
        val proxyUrl = "$PROXY_URL/api/github/releases?owner=Jakeopsjj&repo=ClearFlow&per_page=$PER_PAGE"
        val body = try {
            fetchFromUrl(proxyUrl)
        } catch (e: Exception) {
            Log.w(TAG, "Proxy fetch failed, trying direct GitHub API: ${e.message}")
            try {
                fetchFromUrl("$GITHUB_API_URL?per_page=$PER_PAGE")
            } catch (e2: Exception) {
                Log.e(TAG, "Direct GitHub API also failed: ${e2.message}")
                return emptyList()
            }
        }

        val jsonArray = JSONArray(body)

        return (0 until jsonArray.length()).map { i ->
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
    }

    /**
     * 通用 URL 请求方法，返回响应 body 字符串。
     */
    private fun fetchFromUrl(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
        connection.setRequestProperty("User-Agent", "ClearFlow-App")

        val responseCode = connection.responseCode
        if (responseCode != 200) {
            throw RuntimeException("HTTP $responseCode")
        }

        return connection.inputStream.bufferedReader().use { it.readText() }
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