package com.cleardu.app.data.weather

import android.content.Context
import android.util.Log
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
import java.io.File

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
    val isLoading: Boolean = false,
    /** 上一次的本地背景类型，用于天气变更时触发波纹动画 */
    val previousLocalBackgroundType: WeatherCodeMapper.LocalBackgroundType =
        WeatherCodeMapper.LocalBackgroundType.SUNNY_DAY,
    /** 天气编码是否发生变更（用于触发波纹动画） */
    val isWeatherCodeChanged: Boolean = false
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
 * - WorkManager 定时刷新天气数据（每小时）。
 * - 天气编码变更时触发多波纹背景切换动画。
 *
 * Must call [initialize] once before use (typically in Application.onCreate).
 */
object WeatherBackgroundManager {

    private const val TAG = "WeatherBgManager"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observeJob: Job? = null

    private var weatherRepository: WeatherRepository? = null
    private var imageRepository: PexelsImageRepository? = null
    private var locationHelper: LocationHelper? = null

    private val _state = MutableStateFlow(WeatherBackgroundState())
    val state: StateFlow<WeatherBackgroundState> = _state.asStateFlow()

    private var hasFetched = false
    /** 上一次成功获取的天气编码，用于检测天气变更 */
    private var previousWeatherCode: String = ""

    /**
     * Initialize the manager. Call once from Application or MainActivity.
     * Weather logic runs only once when the app opens (and when weather is enabled).
     * No periodic refresh is performed.
     */
    fun initialize(context: Context, healthDataManager: HealthDataManager) {
        if (weatherRepository != null) return // already initialized

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
                        fetchWeatherWithLocation()
                    }
                } else {
                    _state.value = WeatherBackgroundState(enabled = false)
                    Log.d(TAG, "Weather background disabled, all network stopped")
                }
            }
        }
    }

    /**
     * 使用设备定位获取天气数据。
     * 优先使用 LocationHelper 获取实时定位，失败时回退到默认北京坐标。
     */
    private fun fetchWeatherWithLocation() {
        val helper = locationHelper
        if (helper == null || !helper.hasLocationPermission()) {
            // 无定位权限，使用默认北京坐标
            Log.d(TAG, "No location permission, using default Beijing coords")
            scope.launch { fetchWeatherAndImage(39.921, 116.469) }
            return
        }

        scope.launch {
            // 尝试获取实时定位
            helper.requestSingleUpdate(timeoutMs = 10_000L) { result ->
                when (result) {
                    is LocationHelper.Result.Success -> {
                        Log.d(TAG, "Got location: lat=${result.location.latitude}, lon=${result.location.longitude}")
                        scope.launch { fetchWeatherAndImage(result.location.latitude, result.location.longitude) }
                    }
                    is LocationHelper.Result.CoarseOnly -> {
                        Log.d(TAG, "Got coarse location: lat=${result.location.latitude}, lon=${result.location.longitude}")
                        scope.launch { fetchWeatherAndImage(result.location.latitude, result.location.longitude) }
                    }
                    else -> {
                        Log.w(TAG, "Location failed: $result, using default Beijing coords")
                        scope.launch { fetchWeatherAndImage(39.921, 116.469) }
                    }
                }
            }
        }
    }

    /**
     * Fetch weather data and corresponding background image.
     * Updates [_state] with the result.
     *
     * @param lat 纬度
     * @param lon 经度
     */
    private suspend fun fetchWeatherAndImage(lat: Double = 39.921, lon: Double = 116.469) {
        val repo = weatherRepository ?: return
        val imgRepo = imageRepository ?: return

        _state.value = _state.value.copy(isLoading = true)

        // Step 1: Fetch weather data
        val weather = try {
            repo.fetchWeather(lat, lon)
        } catch (e: Exception) {
            Log.e(TAG, "Weather fetch error: ${e.message}", e)
            null
        }

        if (weather == null) {
            // Weather API failed — use local fallback with system day/night
            val isDay = WeatherCodeMapper.isDayTime()
            val localType = WeatherCodeMapper.getLocalBackgroundType("00", isDay)
            _state.value = _state.value.copy(
                weatherCode = "00",
                isDayTime = isDay,
                imageFile = null,
                localBackgroundType = localType,
                isLoading = false,
                isWeatherCodeChanged = false
            )
            return
        }

        Log.d(TAG, "Weather: code=${weather.weatherCode}, text=${weather.weatherText}, " +
                "temp=${weather.temperatureC}°C, day=${weather.isDayTime}")

        // 检测天气编码是否变更
        val codeChanged = weather.weatherCode.isNotEmpty() &&
                weather.weatherCode != previousWeatherCode &&
                previousWeatherCode.isNotEmpty()
        previousWeatherCode = weather.weatherCode

        val localType = WeatherCodeMapper.getLocalBackgroundType(
            weather.weatherCode, weather.isDayTime
        )

        val previousLocalType = _state.value.localBackgroundType

        // Step 2: Fetch background image via Pexels proxy
        val imageFile = try {
            imgRepo.getWeatherImage(weather.weatherCode, weather.isDayTime)
        } catch (e: Exception) {
            Log.e(TAG, "Image fetch error: ${e.message}", e)
            null
        }

        _state.value = _state.value.copy(
            weatherCode = weather.weatherCode,
            isDayTime = weather.isDayTime,
            imageFile = imageFile,
            localBackgroundType = localType,
            previousLocalBackgroundType = if (codeChanged) previousLocalType else _state.value.previousLocalBackgroundType,
            isWeatherCodeChanged = codeChanged,
            isLoading = false
        )

        Log.d(TAG, "Background updated: image=${imageFile != null}, type=$localType, " +
                "codeChanged=$codeChanged, previousCode=$previousWeatherCode")
    }

    /**
     * 由 WeatherWorker 调用，处理定时拉取的天气数据。
     * 与 fetchWeatherAndImage 逻辑一致，但不需要定位参数（Worker 已获取）。
     */
    fun onWeatherFetched(weather: WeatherInfo) {
        scope.launch {
            val imgRepo = imageRepository
            if (imgRepo == null) {
                Log.w(TAG, "onWeatherFetched: imageRepository not initialized")
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true)

            // 检测天气编码是否变更
            val codeChanged = weather.weatherCode.isNotEmpty() &&
                    weather.weatherCode != previousWeatherCode &&
                    previousWeatherCode.isNotEmpty()
            previousWeatherCode = weather.weatherCode

            val localType = WeatherCodeMapper.getLocalBackgroundType(
                weather.weatherCode, weather.isDayTime
            )

            val previousLocalType = _state.value.localBackgroundType

            // 获取天气背景图
            val imageFile = try {
                imgRepo.getWeatherImage(weather.weatherCode, weather.isDayTime)
            } catch (e: Exception) {
                Log.e(TAG, "Image fetch error in onWeatherFetched: ${e.message}", e)
                null
            }

            _state.value = _state.value.copy(
                weatherCode = weather.weatherCode,
                isDayTime = weather.isDayTime,
                imageFile = imageFile,
                localBackgroundType = localType,
                previousLocalBackgroundType = if (codeChanged) previousLocalType else _state.value.previousLocalBackgroundType,
                isWeatherCodeChanged = codeChanged,
                isLoading = false
            )

            Log.d(TAG, "onWeatherFetched: type=$localType, codeChanged=$codeChanged")
        }
    }

    /**
     * 调度 WorkManager 定时天气刷新（每小时）。
     * 在 Application.onCreate 中调用。
     */
    fun schedulePeriodicRefresh(context: Context) {
        WeatherWorker.schedule(context.applicationContext)
    }

    /**
     * Manually trigger a weather refresh.
     */
    fun refresh() {
        fetchWeatherWithLocation()
    }

    /**
     * 天气编码变更动画完成后重置标志。
     * 由 WeatherBackground composable 在动画完成时调用。
     */
    fun onWeatherCodeAnimationComplete() {
        _state.value = _state.value.copy(isWeatherCodeChanged = false)
        Log.d(TAG, "Weather code change animation complete, flag reset")
    }

    /**
     * Clear the image cache.
     */
    fun clearCache() {
        imageRepository?.clearCache()
    }
}