package com.cleardu.app.data.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * WorkManager CoroutineWorker — 每小时定时拉取风华爱科天气数据。
 *
 * 流程：
 * 1. 从 LocationManager 获取最近一次缓存定位（同步读取，不阻塞主线程）
 * 2. 无缓存时使用默认北京坐标（39.921, 116.469）
 * 3. 调用 WeatherRepository.fetchWeather(lat, lon) 获取天气
 * 4. 将结果写入 WeatherBackgroundManager，触发天气变更检测与背景切换
 * 5. 网络请求失败时静默跳过，不崩溃、不空白
 */
class WeatherWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "===== WeatherWorker doWork() start =====")

        // 1. 获取设备定位（同步读缓存，无缓存用默认北京坐标）
        val (lat, lon) = getLastKnownLocation()

        Log.d(TAG, "Using location: lat=$lat, lon=$lon")

        // 2. 调用天气 API
        val repository = WeatherRepository()
        val weather = try {
            repository.fetchWeather(lat, lon)
        } catch (e: Exception) {
            Log.e(TAG, "Weather fetch failed in worker: ${e.message}", e)
            null
        }

        if (weather == null) {
            Log.w(TAG, "Weather fetch returned null, keeping current background")
            return Result.success() // 网络失败不重试，继续沿用当前背景
        }

        Log.d(TAG, "Weather fetched: code=${weather.weatherCode}, text=${weather.weatherText}, " +
                "temp=${weather.temperatureC}°C, day=${weather.isDayTime}")

        // 3. 更新 WeatherBackgroundManager 状态
        WeatherBackgroundManager.onWeatherFetched(weather)

        Log.i(TAG, "===== WeatherWorker doWork() done =====")
        return Result.success()
    }

    /**
     * 从 LocationManager 同步读取最近一次缓存定位。
     * 优先 GPS，其次 NETWORK。无缓存返回默认北京坐标。
     */
    private fun getLastKnownLocation(): Pair<Double, Double> {
        if (!hasLocationPermission()) {
            Log.w(TAG, "No location permission, using default Beijing coords")
            return DEFAULT_LAT to DEFAULT_LON
        }

        val lm = applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            Log.w(TAG, "LocationManager unavailable, using default Beijing coords")
            return DEFAULT_LAT to DEFAULT_LON
        }

        try {
            val gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val network = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val best = when {
                gps != null && network != null -> if (gps.time >= network.time) gps else network
                gps != null -> gps
                network != null -> network
                else -> null
            }

            if (best != null) {
                val ageMin = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - best.time)
                if (ageMin < MAX_CACHE_AGE_MINUTES) {
                    Log.d(TAG, "Using cached location: lat=${best.latitude}, lon=${best.longitude}, age=${ageMin}min")
                    return best.latitude to best.longitude
                }
                Log.d(TAG, "Cached location too old (${ageMin}min), using default Beijing coords")
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException reading location: ${e.message}")
        }

        return DEFAULT_LAT to DEFAULT_LON
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "WeatherWorker"
        private const val WORK_NAME = "weather_periodic_refresh"

        /** 默认北京坐标 */
        private const val DEFAULT_LAT = 39.921
        private const val DEFAULT_LON = 116.469

        /** 缓存定位最大有效期（分钟） */
        private const val MAX_CACHE_AGE_MINUTES = 120L

        /**
         * 调度每小时定时天气刷新。
         * 重复调用不会创建重复任务（KEEP ExistingPeriodicWorkPolicy）。
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WeatherWorker>(
                repeatInterval = 1, TimeUnit.HOURS,
                flexTimeInterval = 15, TimeUnit.MINUTES // 灵活窗口避免所有设备同时请求
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.i(TAG, "Weather periodic work scheduled: every 1 hour")
        }
    }
}