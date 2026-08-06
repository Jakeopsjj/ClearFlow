package com.cleardu.app.data.pexels

import android.util.Log
import com.cleardu.app.BuildConfig
import com.cleardu.app.data.weather.WeatherCodeMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Repository for fetching weather background images via Pexels proxy.
 *
 * Fallback chain:
 * 1. Check local disk cache (2hr TTL) → if hit, return cached file
 * 2. Try Koyeb proxy → get image URL → download → cache → return file
 * 3. Try Render proxy → get image URL → download → cache → return file
 * 4. All failed → return null (caller uses local Compose-drawn fallback)
 *
 * @param context Application context for cache directory
 */
class PexelsImageRepository(context: android.content.Context) {

    private val cache = WeatherImageCache(context)

    private val koyebApi: PexelsProxyApi
    private val renderApi: PexelsProxyApi

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

        koyebApi = Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(BuildConfig.PEXELS_PROXY_KOYEB_URL))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PexelsProxyApi::class.java)

        renderApi = Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(BuildConfig.PEXELS_PROXY_RENDER_URL))
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

        // Step 2: Try Koyeb (primary)
        val imageUrl = tryFetchImageUrl(koyebApi, keyword, "Koyeb")
            ?: tryFetchImageUrl(renderApi, keyword, "Render") // Step 3: Try Render (backup)

        if (imageUrl.isNullOrBlank()) {
            Log.w(TAG, "All proxies failed, using local fallback")
            return@withContext null
        }

        // Download and cache
        val file = cache.downloadAndCache(imageUrl, weatherCode, isDayTime)
        if (file != null) {
            Log.d(TAG, "Image cached successfully: ${file.name}")
        }
        file
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
