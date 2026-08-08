package com.cleardu.app.data.weather

import android.content.Context
import android.util.Log
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.pexels.PexelsImageRepository
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
    /** Debug mode: null=auto, true=bright, false=dark. Only effective in debug builds. */
    val debugBrightMode: Boolean? = null
) {
    /**
     * 当前背景是否属于高亮度背景（晴/雪/多云白天）。
     * 用于 UI 文字颜色自适应：高亮背景使用深色文字，暗色背景使用浅色文字。
     * Debug 模式下由 debugBrightMode 手动控制。
     */
    val isBrightBackground: Boolean
        get() = debugBrightMode ?: when (localBackgroundType) {
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
 */
object WeatherBackgroundManager {

    private const val TAG = "WeatherBgManager"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observeJob: Job? = null

    private var weatherRepository: WeatherRepository? = null
    private var imageRepository: PexelsImageRepository? = null

    private val _state = MutableStateFlow(WeatherBackgroundState())
    val state: StateFlow<WeatherBackgroundState> = _state.asStateFlow()

    private var hasFetched = false

    /**
     * Initialize the manager. Call once from Application or MainActivity.
     * Weather logic runs only once when the app opens (and when weather is enabled).
     * No periodic refresh is performed.
     */
    fun initialize(context: Context, healthDataManager: HealthDataManager) {
        if (weatherRepository != null) return // already initialized

        weatherRepository = WeatherRepository()
        imageRepository = PexelsImageRepository(context.applicationContext)

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
                } else {
                    _state.value = WeatherBackgroundState(enabled = false)
                    Log.d(TAG, "Weather background disabled, all network stopped")
                }
            }
        }
    }

    /**
     * Fetch weather data and corresponding background image.
     * Updates [_state] with the result.
     */
    private suspend fun fetchWeatherAndImage() {
        val repo = weatherRepository ?: return
        val imgRepo = imageRepository ?: return

        _state.value = _state.value.copy(isLoading = true)

        // Step 1: Fetch weather data
        val weather = try {
            repo.fetchWeather()
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
                isLoading = false
            )
            return
        }

        Log.d(TAG, "Weather: code=${weather.weatherCode}, text=${weather.weatherText}, " +
                "temp=${weather.temperatureC}°C, day=${weather.isDayTime}")

        // Step 2: Fetch background image via Pexels proxy
        val imageFile = try {
            imgRepo.getWeatherImage(weather.weatherCode, weather.isDayTime)
        } catch (e: Exception) {
            Log.e(TAG, "Image fetch error: ${e.message}", e)
            null
        }

        val localType = WeatherCodeMapper.getLocalBackgroundType(
            weather.weatherCode, weather.isDayTime
        )

        _state.value = _state.value.copy(
            weatherCode = weather.weatherCode,
            isDayTime = weather.isDayTime,
            imageFile = imageFile,
            localBackgroundType = localType,
            isLoading = false
        )

        Log.d(TAG, "Background updated: image=${imageFile != null}, type=$localType")
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
     * Debug only: manually set background brightness mode.
     * Swaps the actual background image — bright mode shows a sunny day scene,
     * dark mode shows a night scene. Auto mode reverts to weather-based behavior.
     *
     * @param brightMode null=auto, true=强制亮色背景（晴天白天）, false=强制暗色背景（晴夜）
     */
    fun setDebugBrightMode(brightMode: Boolean?) {
        val newType = when (brightMode) {
            true -> WeatherCodeMapper.LocalBackgroundType.SUNNY_DAY
            false -> WeatherCodeMapper.LocalBackgroundType.SUNNY_NIGHT
            null -> _state.value.localBackgroundType // keep current type, will be overwritten by refresh
        }
        _state.value = _state.value.copy(
            debugBrightMode = brightMode,
            localBackgroundType = newType,
            imageFile = null, // clear image to force new fetch
            weatherCode = if (brightMode == true) "00" else if (brightMode == false) "00" else _state.value.weatherCode,
            isDayTime = brightMode ?: _state.value.isDayTime
        )
        if (brightMode != null) {
            // Fetch a debug-appropriate background image
            scope.launch { fetchDebugImage(brightMode) }
        } else {
            // Revert to auto mode — re-fetch weather-based background
            refresh()
        }
    }

    /**
     * Fetch a background image for debug mode (bright or dark).
     * Uses the Pexels repository to get a matching image without requiring weather data.
     */
    private suspend fun fetchDebugImage(isBright: Boolean) {
        val imgRepo = imageRepository ?: return

        _state.value = _state.value.copy(isLoading = true)

        val weatherCode = "00" // sunny
        val isDayTime = isBright

        val imageFile = try {
            imgRepo.getWeatherImage(weatherCode, isDayTime)
        } catch (e: Exception) {
            Log.e(TAG, "Debug image fetch error: ${e.message}", e)
            null
        }

        val localType = if (isBright) {
            WeatherCodeMapper.LocalBackgroundType.SUNNY_DAY
        } else {
            WeatherCodeMapper.LocalBackgroundType.SUNNY_NIGHT
        }

        _state.value = _state.value.copy(
            weatherCode = weatherCode,
            isDayTime = isDayTime,
            imageFile = imageFile,
            localBackgroundType = localType,
            isLoading = false
        )

        Log.d(TAG, "Debug background: bright=$isBright, image=${imageFile != null}, type=$localType")
    }
}
