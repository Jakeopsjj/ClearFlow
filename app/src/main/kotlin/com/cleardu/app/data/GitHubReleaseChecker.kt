package com.cleardu.app.data

import android.util.Log
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
 * - Debug 通道：仅匹配包含 "-debug" 后缀的 Release
 * - Release 通道：仅匹配不含 "-debug" 后缀的 Release
 * - 版本比较：使用语义化版本（major.minor.patch）比较，仅当远端版本严格高于当前版本时提示更新
 */
object GitHubReleaseChecker {

    private const val TAG = "GitHubReleaseChecker"
    private const val GITHUB_API_URL = "https://api.github.com/repos/Jakeopsjj/ClearFlow/releases"
    private const val TIMEOUT_MS = 10_000
    private const val PER_PAGE = 30

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

            // 按通道过滤：Debug 通道匹配包含 "-debug" 的版本，Release 通道匹配不含 "-debug" 的版本
            val channelReleases = releases.filter { release ->
                val isDebugRelease = release.versionName.contains("-debug") ||
                        release.tagName.contains("-debug")
                if (isDebug) isDebugRelease else !isDebugRelease
            }

            if (channelReleases.isEmpty()) {
                Log.w(TAG, "No releases found for ${if (isDebug) "Debug" else "Release"} channel")
                return@withContext null
            }

            // 返回版本号最高的 Release（按解析后的版本号排序）
            channelReleases.maxByOrNull { release ->
                val parts = release.parsedVersion
                (parts.getOrElse(0) { 0 }) * 1_000_000L +
                    (parts.getOrElse(1) { 0 }) * 1_000L +
                    (parts.getOrElse(2) { 0 })
            }
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
        val url = URL("$GITHUB_API_URL?per_page=$PER_PAGE")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
        connection.setRequestProperty("User-Agent", "ClearFlow-App")

        val responseCode = connection.responseCode
        if (responseCode != 200) {
            Log.w(TAG, "GitHub API returned $responseCode")
            return emptyList()
        }

        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(body)

        return (0 until jsonArray.length()).map { i ->
            val json = jsonArray.getJSONObject(i)
            ReleaseInfo(
                tagName = json.optString("tag_name", ""),
                versionName = json.optString("tag_name", "").removePrefix("v"),
                body = json.optString("body", ""),
                publishedAt = json.optString("published_at", ""),
                htmlUrl = json.optString("html_url", ""),
                apkUrl = json.optJSONArray("assets")?.let { assets ->
                    (0 until assets.length()).mapNotNull { i ->
                        val asset = assets.getJSONObject(i)
                        asset.optString("browser_download_url", "")
                    }.firstOrNull { it.endsWith(".apk") }
                } ?: ""
            )
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
 * GitHub Release 信息。
 */
data class ReleaseInfo(
    val tagName: String,       // e.g. "v1.10.0"
    val versionName: String,   // e.g. "1.10.0"
    val body: String,          // release notes (markdown)
    val publishedAt: String,   // ISO 8601 timestamp
    val htmlUrl: String,       // GitHub release page URL
    val apkUrl: String         // APK download URL
)