package com.cleardu.app.data.weather

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cleardu.app.util.LocationHelper
import kotlinx.coroutines.withTimeoutOrNull

/**
 * [需求4] WeatherSyncWorker — WorkManager 定时天气同步任务。
 *
 * 由 [WeatherBackgroundManager] 调度，每小时执行一次：
 * 1. 通过 [LocationHelper] 获取设备定位
 * 2. 调用风华爱科天气 API 获取最新天气
 * 3. 仅当天气状态变更时更新背景图，触发多波纹动画
 * 4. 失败时沿用当前背景，不空白、不崩溃
 *
 * Worker 自身只负责触发 [WeatherBackgroundManager.refresh]，
 * 具体的天气检测、动画触发、亮度联动都在 Manager 中处理，
 * 确保 Worker 与前台逻辑共用同一套状态机。
 *
 * 约束：仅在网络可用时执行（由 [WeatherBackgroundManager.scheduleWeatherSyncWork] 设置）。
 */
class WeatherSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "WeatherSyncWorker triggered, refreshing weather")

        return try {
            // 检查天气背景开关是否仍然开启
            val state = WeatherBackgroundManager.state.value
            if (!state.enabled) {
                Log.d(TAG, "Weather background disabled, skipping sync")
                return Result.success()
            }

            // 触发天气刷新（包含定位、天气拉取、背景切换动画）
            WeatherBackgroundManager.refresh()

            // 等待刷新完成（最长 30 秒），避免 Worker 过早结束导致状态不一致
            withTimeoutOrNull(30_000L) {
                // 简单等待 isLoading 从 true 变 false
                var waited = 0
                while (WeatherBackgroundManager.state.value.isLoading && waited < 30) {
                    kotlinx.coroutines.delay(1000L)
                    waited++
                }
            }

            Log.d(TAG, "WeatherSyncWorker completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "WeatherSyncWorker failed: ${e.message}", e)
            // 失败时返回 success 而非 retry，避免 WorkManager 频繁重试耗电
            // 下一个周期会自然重试
            Result.success()
        }
    }

    companion object {
        private const val TAG = "WeatherSyncWorker"
    }
}
