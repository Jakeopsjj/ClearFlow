package com.cleardu.app.data.weather

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.pexels.PexelsImageRepository
import com.cleardu.app.util.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * State of the weather background feature.
 */
data class WeatherBackgroundState(
    val enabled: Boolean = false,
    val weatherCode: String = "00",
    val isDayTime: Boolean = true,
    val imageFile: File? = null,
    val localBackgroundType: WeatherCodeMapper.LocalBackgroundType =
        WeatherCodeMapper.LocalBackgroundType.SUNNY_DAY,
    val isLoading: Boolean = false
) {
    /**
     * 当前背景是否属于高亮度背景（晴/雪/多云白天）。
     * 用于 UI 文字颜色自适应：高亮背景使用深色文字，暗色背景使用浅色文字。
     */
    val isBrightBackground: Boolean
        get() = when (localBackgroundType) {
            WeatherCodeMapper.LocalBackgroundType.SUNNY_DAY,
            WeatherCodeMapper.LocalBackgroundType.CLOUDY_DAY,
            WeatherCodeMapper.LocalBackgroundType.SNOW_DAY,
            WeatherCodeMapper.LocalBackgroundType.FOG,
            WeatherCodeMapper.LocalBackgroundType.SANDSTORM -> true
            else -> false
        }
}

/**
 * Singleton manager for weather background functionality.
 *
 * - Observes [HealthDataManager.settings] for the weather background toggle.
 * - When enabled: fetches weather data → fetches Pexels image → updates state.
 * - When disabled: stops ALL network activity, uses local fallback only.
 * - All screens observe [state] for real-time UI updates.
 *
 * Must call [initialize] once before use (typically in Application.onCreate).
 *
 * ## 需求4：定位对接 + WorkManager 定时刷新 + 天气变更触发动画
 *
 * - 使用 [LocationHelper] 获取设备实际坐标，调用风华爱科天气 API
 * - 通过 [WeatherSyncWorker] 实现每小时定时拉取天气数据
 * - 仅当天气状态（weatherCode + isDayTime）发生真实变更时才更新 [imageFile]，
 *   触发多波纹背景切换动画；天气无变化不触发动画
 * - 网络请求失败时继续沿用当前背景，不空白、不崩溃
 * - 背景切换完成自动通过 [state] 触发亮度联动 UI 更新
 */
object WeatherBackgroundManager {

    private const val TAG = "WeatherBgManager"

    /** [修改点-需求4] WorkManager 唯一任务名，用于定时拉取天气。 */
    private const val WEATHER_SYNC_WORK_NAME = "weather_sync_periodic_work"

    /** [修改点-需求4] 定时拉取间隔（分钟），需求要求每小时一次。 */
    private const val SYNC_INTERVAL_MINUTES = 60L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observeJob: Job? = null

    private var appContext: Context? = null
    private var weatherRepository: WeatherRepository? = null
    private var imageRepository: PexelsImageRepository? = null
    private var locationHelper: LocationHelper? = null

    private val _state = MutableStateFlow(WeatherBackgroundState())
    val state: StateFlow<WeatherBackgroundState> = _state.asStateFlow()

    private var hasFetched = false

    /**
     * Initialize the manager. Call once from Application or MainActivity.
     *
     * Weather logic runs once when the app opens (and when weather is enabled),
     * then scheduled via [WeatherSyncWorker] for hourly refresh.
     */
    fun initialize(context: Context, healthDataManager: HealthDataManager) {
        if (weatherRepository != null) return // already initialized

        appContext = context.applicationContext
        weatherRepository = WeatherRepository()
        imageRepository = PexelsImageRepository(context.applicationContext)
        locationHelper = LocationHelper.create(context.applicationContext)

        // Observe settings for weather background toggle changes
        observeJob?.cancel()
        observeJob = scope.launch {
            healthDataManager.settings.collectLatest { settings ->
                if (settings.weatherBackgroundEnabled) {
                    val wasDisabled = !_state.value.enabled
                    _state.value = _state.value.copy(enabled = true)
                    // 首次打开 App 或开关从关闭变为开启时，重新获取天气
                    if (!hasFetched || wasDisabled) {
                        hasFetched = true
                        fetchWeatherAndImage()
                    }
                    // [修改点-需求4] 启用天气背景时，启动 WorkManager 每小时定时拉取
                    scheduleWeatherSyncWork()
                } else {
                    _state.value = WeatherBackgroundState(enabled = false)
                    // [修改点-需求4] 关闭天气背景时，取消定时任务
                    cancelWeatherSyncWork()
                    Log.d(TAG, "Weather background disabled, all network stopped")
                }
            }
        }
    }

    /**
     * Fetch weather data and corresponding background image.
     * Updates [_state] with the result.
     *
     * [修改点-需求4] 关键改动：
     * 1. 通过 [LocationHelper] 获取设备实际坐标，传入天气 API
     * 2. 仅当 weatherCode + isDayTime 真实变更时才更新 imageFile（触发动画）
     * 3. 网络请求失败时保留当前背景，不清空 imageFile
     */
    private suspend fun fetchWeatherAndImage() {
        val repo = weatherRepository ?: return
        val imgRepo = imageRepository ?: return
        val locator = locationHelper

        _state.value = _state.value.copy(isLoading = true)

        // [修改点-需求4] Step 0: 获取设备定位（最多等待 10 秒）
        val location: Location? = if (locator != null) {
            withTimeoutOrNull(10_000L) { fetchDeviceLocation(locator) }
        } else null

        val lat = location?.latitude ?: 39.921
        val lon = location?.longitude ?: 116.469
        Log.d(TAG, "Location: lat=$lat, lon=$lon (source=${if (location != null) "device" else "default"})")

        // Step 1: Fetch weather data (传入设备坐标)
        val weather = try {
            repo.fetchWeather(lat, lon)
        } catch (e: Exception) {
            Log.e(TAG, "Weather fetch error: ${e.message}", e)
            null
        }

        if (weather == null) {
            // [修改点-需求4] 网络请求失败时继续沿用当前背景，不空白、不崩溃
            // 仅首次启动（无任何背景时）才回退到本地默认背景
            if (_state.value.imageFile == null && _state.value.weatherCode == "00") {
                val isDay = WeatherCodeMapper.isDayTime()
                val localType = WeatherCodeMapper.getLocalBackgroundType("00", isDay)
                _state.value = _state.value.copy(
                    weatherCode = "00",
                    isDayTime = isDay,
                    localBackgroundType = localType,
                    isLoading = false
                )
            } else {
                Log.d(TAG, "Weather fetch failed, keeping current background")
                _state.value = _state.value.copy(isLoading = false)
            }
            return
        }

        Log.d(TAG, "Weather: code=${weather.weatherCode}, text=${weather.weatherText}, " +
                "temp=${weather.temperatureC}°C, day=${weather.isDayTime}")

        // [修改点-需求4] Step 2: 检测天气状态是否真实变更
        // 仅当 weatherCode + isDayTime 变化时才更新 imageFile，触发多波纹动画
        val weatherChanged = _state.value.weatherCode != weather.weatherCode ||
            _state.value.isDayTime != weather.isDayTime

        val newLocalType = WeatherCodeMapper.getLocalBackgroundType(
            weather.weatherCode, weather.isDayTime
        )

        if (!weatherChanged) {
            // 天气无变化，不触发动画；仅刷新温度等非视觉字段
            Log.d(TAG, "Weather unchanged (code=${weather.weatherCode}), no animation triggered")
            _state.value = _state.value.copy(
                weatherCode = weather.weatherCode,
                isDayTime = weather.isDayTime,
                localBackgroundType = newLocalType,
                isLoading = false
            )
            return
        }

        // 天气发生真实变更，拉取新背景图，触发多波纹动画
        val imageFile = try {
            imgRepo.getWeatherImage(weather.weatherCode, weather.isDayTime)
        } catch (e: Exception) {
            Log.e(TAG, "Image fetch error: ${e.message}", e)
            null
        }

        // [修改点-需求4] 即使图片拉取失败，也更新 weatherCode 和 localBackgroundType
        // 图片失败时 imageFile 保留旧值（如有），不空白
        _state.value = _state.value.copy(
            weatherCode = weather.weatherCode,
            isDayTime = weather.isDayTime,
            imageFile = imageFile ?: _state.value.imageFile,
            localBackgroundType = newLocalType,
            isLoading = false
        )

        Log.d(TAG, "Background updated: weatherChanged=true, image=${imageFile != null}, type=$newLocalType")
    }

    /**
     * [修改点-需求4] 通过 [LocationHelper] 获取设备定位。
     * 挂起协程，等待回调结果。
     */
    private suspend fun fetchDeviceLocation(locator: LocationHelper): Location? =
        suspendCancellableCoroutine { cont ->
            locator.requestSingleUpdate(timeoutMs = 8000L) { result ->
                when (result) {
                    is LocationHelper.Result.Success -> {
                        if (cont.isActive) cont.resume(result.location)
                    }
                    is LocationHelper.Result.CoarseOnly -> {
                        // 粗定位也接受，优于默认坐标
                        if (cont.isActive) cont.resume(result.location)
                    }
                    else -> {
                        // PermissionDenied / Timeout / LocationDisabled / NoManager
                        Log.w(TAG, "Location fetch result: $result")
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
            // 协程取消时注销监听
            cont.invokeOnCancellation {
                locator.destroy()
            }
        }

    /**
     * Manually trigger a weather refresh.
     */
    fun refresh() {
        scope.launch { fetchWeatherAndImage() }
    }

    /**
     * Clear the image cache.
     */
    fun clearCache() {
        imageRepository?.clearCache()
    }

    /**
     * [修改点-需求4] 调度每小时定时天气同步任务。
     *
     * 使用 [ExistingPeriodicWorkPolicy.KEEP] 避免重复创建任务。
     * 约束：需要网络连接（任意类型）。
     */
    private fun scheduleWeatherSyncWork() {
        val ctx = appContext ?: return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 15 分钟是 WorkManager 最小周期，但需求要求每小时一次
        val workRequest = PeriodicWorkRequestBuilder<WeatherSyncWorker>(
            SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            WEATHER_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Log.d(TAG, "Weather sync work scheduled (interval=${SYNC_INTERVAL_MINUTES}min)")
    }

    /**
     * [修改点-需求4] 取消定时天气同步任务。
     */
    private fun cancelWeatherSyncWork() {
        val ctx = appContext ?: return
        WorkManager.getInstance(ctx).cancelUniqueWork(WEATHER_SYNC_WORK_NAME)
        Log.d(TAG, "Weather sync work cancelled")
    }
}
