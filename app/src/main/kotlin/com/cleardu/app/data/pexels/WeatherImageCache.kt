package com.cleardu.app.data.pexels

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Disk cache for weather background images.
 *
 * - Cache directory: context.cacheDir/weather_backgrounds/
 * - File naming: {weatherCode}_{dayOrNight}.jpg
 * - TTL: 2 hours (check file.lastModified())
 *
 * When cache hit and not expired: return cached file.
 * When cache miss or expired: download from URL and save.
 */
class WeatherImageCache(private val context: Context) {

    private val cacheDir = File(context.cacheDir, "weather_backgrounds").apply {
        if (!exists()) mkdirs()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Get cached image file for the given weather code and day/night status.
     * Returns the local File if cache is valid, null otherwise.
     */
    fun getCachedFile(weatherCode: String, isDayTime: Boolean): File? {
        val file = getCacheFile(weatherCode, isDayTime)
        if (!file.exists() || file.length() == 0L) return null

        val ageMs = System.currentTimeMillis() - file.lastModified()
        if (ageMs > CACHE_TTL_MS) {
            Log.d(TAG, "Cache expired for ${file.name}, age=${ageMs / 1000}s")
            return null
        }

        Log.d(TAG, "Cache hit for ${file.name}")
        return file
    }

    /**
     * Download image from URL and save to cache.
     * Returns the saved file on success, null on failure.
     */
    fun downloadAndCache(
        imageUrl: String,
        weatherCode: String,
        isDayTime: Boolean
    ): File? {
        return try {
            val request = Request.Builder().url(imageUrl).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "Image download failed: HTTP ${response.code}")
                return null
            }

            val file = getCacheFile(weatherCode, isDayTime)
            response.body?.byteStream()?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (file.length() > 0) {
                Log.d(TAG, "Image cached: ${file.name}, size=${file.length()}")
                file
            } else {
                file.delete()
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Image download failed: ${e.message}", e)
            null
        }
    }

    /**
     * Clear all cached weather images.
     */
    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
        Log.d(TAG, "Weather image cache cleared")
    }

    private fun getCacheFile(weatherCode: String, isDayTime: Boolean): File {
        val dayNight = if (isDayTime) "day" else "night"
        return File(cacheDir, "${weatherCode}_${dayNight}.jpg")
    }

    companion object {
        private const val TAG = "WeatherImageCache"
        private val CACHE_TTL_MS = 2 * 60 * 60 * 1000L // 2 hours
    }
}
