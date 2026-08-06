package com.cleardu.app.data.pexels

import android.util.Log
import com.cleardu.app.BuildConfig
import com.cleardu.app.data.weather.WeatherCodeMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Repository for fetching weather background images via Pexels proxy.
 *
 * Fallback chain:
 * 1. Check local disk cache (2hr TTL) → if hit, return cached file
 * 2. Try Koyeb proxy → get image URL → download via proxy → cache → return file
 * 3. Try Render proxy → get image URL → download via proxy → cache → return file
 * 4. All failed → return null (caller uses local built-in fallback)
 *
 * Image download also goes through the proxy server to avoid
 * Pexels CDN being blocked in certain regions.
 *
 * @param context Application context for cache directory
 */
class PexelsImageRepository(context: android.content.Context) {

    private val cache = WeatherImageCache(context)

    private val koyebApi: PexelsProxyApi
    private val renderApi: PexelsProxyApi
    private val koyebBaseUrl: String
    private val renderBaseUrl: String

    private val downloadClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        koyebBaseUrl = ensureTrailingSlash(BuildConfig.PEXELS_PROXY_KOYEB_URL)
        renderBaseUrl = ensureTrailingSlash(BuildConfig.PEXELS_PROXY_RENDER_URL)

        koyebApi = Retrofit.Builder()
            .baseUrl(koyebBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PexelsProxyApi::class.java)

        renderApi = Retrofit.Builder()
            .baseUrl(renderBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PexelsProxyApi::class.java)
    }

    /**
     * Get a weather background image for the given weather code and day/night status.
     *
     * @return local File of the image, or null if all sources failed
     */
    suspend fun getWeatherImage(
        weatherCode: String,
        isDayTime: Boolean
    ): File? = withContext(Dispatchers.IO) {
        // Step 1: Check local cache (2hr TTL)
        cache.getCachedFile(weatherCode, isDayTime)?.let { return@withContext it }

        val keyword = WeatherCodeMapper.getPexelsSearchKeyword(weatherCode, isDayTime)
        Log.d(TAG, "Fetching: code=$weatherCode, day=$isDayTime, keyword=$keyword")

        // Step 2: Try Koyeb (primary) — get URL + download via proxy
        var file = tryFetchAndDownload(koyebApi, koyebBaseUrl, keyword, weatherCode, isDayTime, "Koyeb")
        if (file != null) return@withContext file

        // Step 3: Try Render (backup) — get URL + download via proxy
        file = tryFetchAndDownload(renderApi, renderBaseUrl, keyword, weatherCode, isDayTime, "Render")
        if (file != null) return@withContext file

        Log.w(TAG, "All proxies failed, using local fallback")
        null
    }

    /**
     * Try to fetch image URL from proxy, then download via proxy.
     * Returns cached file on success, null on failure.
     */
    private suspend fun tryFetchAndDownload(
        api: PexelsProxyApi,
        baseUrl: String,
        keyword: String,
        weatherCode: String,
        isDayTime: Boolean,
        label: String
    ): File? {
        // Step A: Get image URL from proxy
        val imageUrl = tryFetchImageUrl(api, keyword, label) ?: return null

        // Step B: Try downloading directly first
        val file = cache.downloadAndCache(imageUrl, weatherCode, isDayTime)
        if (file != null) {
            Log.d(TAG, "$label: direct download OK")
            return file
        }

        // Step C: Direct download failed, try via proxy
        Log.d(TAG, "$label: direct download failed, trying via proxy")
        return downloadViaProxy(baseUrl, imageUrl, weatherCode, isDayTime, label)
    }

    /**
     * Download image through the proxy server (to bypass CDN blocking).
     */
    private fun downloadViaProxy(
        baseUrl: String,
        imageUrl: String,
        weatherCode: String,
        isDayTime: Boolean,
        label: String
    ): File? {
        return try {
            val encodedUrl = URLEncoder.encode(imageUrl, "UTF-8")
            val proxyDownloadUrl = "${baseUrl}api/image?url=$encodedUrl"
            val request = Request.Builder().url(proxyDownloadUrl).build()
            val response = downloadClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "$label: proxy download HTTP ${response.code}")
                return null
            }

            val file = cache.getCacheFile(weatherCode, isDayTime)
            response.body?.byteStream()?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (file.length() > 0) {
                Log.d(TAG, "$label: proxy download OK, size=${file.length()}")
                file
            } else {
                file.delete()
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "$label: proxy download failed: ${e.message}")
            null
        }
    }

    /**
     * Try to get an image URL from a proxy. Returns null on any failure.
     */
    private suspend fun tryFetchImageUrl(
        api: PexelsProxyApi,
        keyword: String,
        label: String
    ): String? {
        return try {
            val response = withTimeoutOrNull(12_000L) {
                api.searchPhoto(keyword)
            }

            if (response == null) {
                Log.w(TAG, "$label: timeout or null response")
                return null
            }

            if (response.imageUrl.isNullOrBlank()) {
                Log.w(TAG, "$label: no image URL, error=${response.error}")
                return null
            }

            Log.d(TAG, "$label: got image URL")
            response.imageUrl
        } catch (e: Exception) {
            Log.w(TAG, "$label failed: ${e.message}")
            null
        }
    }

    fun clearCache() {
        cache.clearCache()
    }

    private fun ensureTrailingSlash(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }

    companion object {
        private const val TAG = "PexelsImageRepo"
    }
}
