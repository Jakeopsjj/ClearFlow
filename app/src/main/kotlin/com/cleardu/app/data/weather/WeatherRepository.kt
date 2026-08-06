package com.cleardu.app.data.weather

import android.util.Log
import com.cleardu.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Repository for fetching weather data from 华风爱科 API.
 *
 * Two-step flow:
 * 1. Search location by coordinates → get locationKey
 * 2. Get current conditions by locationKey → get WeatherCode + temperature
 *
 * Default location: Beijing (39.921, 116.469)
 */
class WeatherRepository {

    private val api: WeatherApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.weathercn.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(WeatherApiService::class.java)
    }

    /**
     * Fetch current weather for the default location (Beijing).
     * Returns null on any error or timeout.
     *
     * @param lat latitude (default: Beijing 39.921)
     * @param lon longitude (default: Beijing 116.469)
     */
    suspend fun fetchWeather(
        lat: Double = 39.921,
        lon: Double = 116.469
    ): WeatherInfo? = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.WEATHER_API_KEY
            if (apiKey.isBlank()) {
                Log.w(TAG, "WEATHER_API_KEY is empty, skipping weather fetch")
                return@withContext null
            }

            // Round to 3 decimal places as required by API guidelines
            val q = String.format("%.3f,%.3f", lat, lon)

            // Step 1: Get location key (with 10s timeout)
            val location = withTimeoutOrNull(10_000L) {
                api.searchLocation(apiKey, q)
            }

            if (location == null || location.key.isBlank()) {
                Log.w(TAG, "Failed to get location key")
                return@withContext null
            }

            // Step 2: Get current conditions (with 15s timeout)
            val conditions = withTimeoutOrNull(15_000L) {
                api.getCurrentConditions(location.key, apiKey)
            }

            if (conditions.isNullOrEmpty()) {
                Log.w(TAG, "No current conditions returned")
                return@withContext null
            }

            val cond = conditions[0]
            val weatherCode = cond.localSource?.weatherCode ?: "00"
            val tempC = cond.temperature?.metric?.value ?: 0.0

            WeatherInfo(
                weatherCode = weatherCode,
                weatherText = cond.weatherText.ifBlank { "未知" },
                isDayTime = cond.isDayTime,
                temperatureC = tempC,
                humidity = cond.relativeHumidity,
                uvIndex = cond.uvIndex,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Weather fetch failed: ${e.message}", e)
            null
        }
    }

    companion object {
        private const val TAG = "WeatherRepository"
    }
}
