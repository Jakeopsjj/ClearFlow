package com.cleardu.app.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * GitHub Release 版本检查器。
 *
 * 通过 GitHub API 获取仓库最新 Release 的版本号、更新日志和下载链接。
 * 用于设置页的「检查更新」功能和首次启动后的更新日志弹窗。
 */
object GitHubReleaseChecker {

    private const val TAG = "GitHubReleaseChecker"
    private const val GITHUB_API_URL = "https://api.github.com/repos/Jakeopsjj/ClearFlow/releases/latest"
    private const val TIMEOUT_MS = 10_000

    /**
     * 获取最新 Release 信息。
     * @return [ReleaseInfo] 或 null（网络错误/解析失败）
     */
    suspend fun fetchLatestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "ClearFlow-App")

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                Log.w(TAG, "GitHub API returned $responseCode")
                return@withContext null
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)

            ReleaseInfo(
                tagName = json.optString("tag_name", ""),
                versionName = json.optString("name", "").ifBlank {
                    json.optString("tag_name", "").removePrefix("v")
                },
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch latest release: ${e.message}", e)
            null
        }
    }
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