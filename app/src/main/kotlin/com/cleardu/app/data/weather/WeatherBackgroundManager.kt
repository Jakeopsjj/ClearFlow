package com.cleardu.app.data.weather

import android.content.Context
import android.util.Log
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.pexels.PexelsImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
    val isLoading: Boolean = false
)

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
    private const val REFRESH_INTERVAL_MS = 30L * 60 * 1000 // 30 minutes

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observeJob: Job? = null

    private var weatherRepository: WeatherRepository? = null
    private var imageRepository: PexelsImageRepository? = null

    private val _state = MutableStateFlow(WeatherBackgroundState())
    val state: StateFlow<WeatherBackgroundState> = _state.asStateFlow()

    /**
     * Initialize the manager. Call once from Application or MainActivity.
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
                    _state.value = _state.value.copy(enabled = true)
                    fetchWeatherAndImage()
                    startPeriodicRefresh(healthDataManager)
                } else {
                    stopPeriodicRefresh()
                    _state.value = WeatherBackgroundState(enabled = false)
                    Log.d(TAG, "Weather background disabled, all network stopped")
                }
            }
        }
    }

    private var refreshJob: Job? = null

    private fun startPeriodicRefresh(healthDataManager: HealthDataManager) {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                fetchWeatherAndImage()
            }
        }
    }

    private fun stopPeriodicRefresh() {
        refreshJob?.cancel()
        refreshJob = null
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
}
