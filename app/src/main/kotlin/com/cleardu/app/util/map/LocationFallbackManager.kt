package com.cleardu.app.util.map

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 定位降级管理器。
 *
 * 降级顺序：高德定位 → 百度定位 → 腾讯定位 → 系统原生定位。
 *
 * 串行自动静默降级，不给用户弹窗报错、toast 提示。
 * 拿到有效结果立刻终止后续调用；全部服务商失败返回可控业务状态。
 * 禁止并发调用多家接口。
 *
 * 页面销毁立刻中断全部异步定位请求。
 */
object LocationFallbackManager {

    private const val TAG = "LocationFallbackManager"
    private const val LOCATION_TIMEOUT_MS = 15_000L

    /** 定位 Job，用于取消 */
    @Volatile
    private var locationJob: Job? = null

    /** 是否正在定位中 */
    private val _locating = MutableStateFlow(false)
    val locating: StateFlow<Boolean> = _locating.asStateFlow()

    /** 定位结果 */
    sealed class LocationResult {
        data class Success(
            val lat: Double,
            val lng: Double,
            val accuracy: Float,
            val vendor: String
        ) : LocationResult()

        data object Timeout : LocationResult()
        data object AllFailed : LocationResult()
    }

    /**
     * 获取当前位置。
     *
     * @param context Android Context
     * @return 定位结果，全部失败返回 AllFailed
     */
    suspend fun getLocation(context: Context): LocationResult = withContext(Dispatchers.IO) {
        Log.i(TAG, "===== 开始获取位置 =====")
        _locating.value = true

        try {
            // 降级顺序：高德 → 百度 → 腾讯 → 系统原生
            val priority = listOf(MapVendor.AMAP, MapVendor.BAIDU, MapVendor.TENCENT)

            for (vendor in priority) {
                if (!isVendorAvailable(vendor)) {
                    Log.d(TAG, "$vendor 定位不可用，跳过")
                    continue
                }

                Log.i(TAG, "尝试 $vendor 定位...")
                val result = try {
                    withTimeout(LOCATION_TIMEOUT_MS) {
                        when (vendor) {
                            MapVendor.AMAP -> locateAmap(context)
                            MapVendor.BAIDU -> locateBaidu(context)
                            MapVendor.TENCENT -> locateTencent(context)
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.e(TAG, "$vendor 定位超时")
                    null
                } catch (e: CancellationException) {
                    Log.w(TAG, "$vendor 定位被取消")
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "$vendor 定位异常", e)
                    null
                }

                if (result != null) {
                    Log.i(TAG, "【数据来源=${(result as LocationResult.Success).vendor}】定位成功，终止后续调用")
                    return@withContext result
                }
            }

            // 最后回退到系统原生定位
            Log.i(TAG, "所有 SDK 定位失败，回退到系统原生定位")
            val nativeResult = try {
                withTimeout(LOCATION_TIMEOUT_MS) {
                    locateNative(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "系统原生定位异常", e)
                null
            }

            if (nativeResult != null) {
                Log.i(TAG, "【数据来源=系统原生】定位成功")
                return@withContext nativeResult
            }

            Log.w(TAG, "所有定位方式均失败")
            LocationResult.AllFailed
        } finally {
            _locating.value = false
        }
    }

    /**
     * 取消当前定位。
     */
    fun cancel() {
        locationJob?.cancel()
        locationJob = null
        _locating.value = false
        Log.i(TAG, "定位已取消")
    }

    // ==================== 高德定位 ====================

    private suspend fun locateAmap(context: Context): LocationResult? =
        suspendCancellableCoroutine { cont ->
            try {
                val client = com.amap.api.location.AMapLocationClient(context.applicationContext)
                val option = com.amap.api.location.AMapLocationClientOption().apply {
                    locationMode = com.amap.api.location.AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                    isOnceLocation = true
                    isOnceLocationLatest = true
                    isNeedAddress = false
                    httpTimeOut = LOCATION_TIMEOUT_MS
                }

                client.setLocationOption(option)
                client.setLocationListener { location ->
                    if (cont.isCancelled) return@setLocationListener
                    try {
                        if (location != null && location.errorCode == 0) {
                            val result = LocationResult.Success(
                                lat = location.latitude,
                                lng = location.longitude,
                                accuracy = location.accuracy,
                                vendor = "高德"
                            )
                            cont.resumeWith(Result.success(result))
                        } else {
                            Log.w(TAG, "高德定位失败: errorCode=${location?.errorCode}, errorInfo=${location?.errorInfo}")
                            cont.resumeWith(Result.success(null))
                        }
                    } catch (e: Exception) {
                        cont.resumeWith(Result.success(null))
                    } finally {
                        client.onDestroy()
                    }
                }

                client.startLocation()

                cont.invokeOnCancellation {
                    client.onDestroy()
                }
            } catch (e: Exception) {
                Log.e(TAG, "高德定位初始化失败", e)
                cont.resumeWith(Result.success(null))
            }
        }

    // ==================== 百度定位 ====================

    private suspend fun locateBaidu(context: Context): LocationResult? =
        suspendCancellableCoroutine { cont ->
            try {
                val client = com.baidu.location.LocationClient(context.applicationContext)
                val option = com.baidu.location.LocationClientOption().apply {
                    locationMode = com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy
                    isOnceLocation = true
                    setIsNeedAddress(false)
                    setTimeOut(LOCATION_TIMEOUT_MS.toInt() / 1000)
                    setIsNeedLocationDescribe(false)
                }

                client.locOption = option

                client.registerLocationListener { location ->
                    if (cont.isCancelled) return@registerLocationListener
                    try {
                        if (location != null && location.latitude > 0 && location.longitude > 0) {
                            // 百度返回的是 BD-09 坐标，转换为 GCJ-02
                            val (gcjLat, gcjLng) = CoordinateConverter.bd09ToGcj02(
                                location.latitude, location.longitude
                            )
                            val result = LocationResult.Success(
                                lat = gcjLat,
                                lng = gcjLng,
                                accuracy = location.radius,
                                vendor = "百度"
                            )
                            cont.resumeWith(Result.success(result))
                        } else {
                            Log.w(TAG, "百度定位失败: locType=${location?.locType}")
                            cont.resumeWith(Result.success(null))
                        }
                    } catch (e: Exception) {
                        cont.resumeWith(Result.success(null))
                    } finally {
                        client.stop()
                    }
                }

                client.start()

                cont.invokeOnCancellation {
                    client.stop()
                }
            } catch (e: Exception) {
                Log.e(TAG, "百度定位初始化失败", e)
                cont.resumeWith(Result.success(null))
            }
        }

    // ==================== 腾讯定位 ====================

    private suspend fun locateTencent(context: Context): LocationResult? =
        suspendCancellableCoroutine { cont ->
            try {
                val tencentLoc = com.tencent.map.geolocation.TencentLocationManager.getInstance(
                    context.applicationContext
                )
                tencentLoc.setCoordinateType(
                    com.tencent.map.geolocation.TencentLocationManager.COORDINATE_TYPE_GCJ02
                )

                val listener = object : com.tencent.map.geolocation.TencentLocationListener {
                    override fun onLocationChanged(
                        location: com.tencent.map.geolocation.TencentLocation?,
                        errorCode: Int,
                        errorMsg: String?
                    ) {
                        if (cont.isCancelled) return
                        try {
                            if (errorCode == com.tencent.map.geolocation.TencentLocation.ERROR_OK &&
                                location != null
                            ) {
                                val result = LocationResult.Success(
                                    lat = location.latitude,
                                    lng = location.longitude,
                                    accuracy = location.accuracy,
                                    vendor = "腾讯"
                                )
                                cont.resumeWith(Result.success(result))
                            } else {
                                Log.w(TAG, "腾讯定位失败: errorCode=$errorCode, errorMsg=$errorMsg")
                                cont.resumeWith(Result.success(null))
                            }
                        } catch (e: Exception) {
                            cont.resumeWith(Result.success(null))
                        }
                    }

                    override fun onStatusUpdate(
                        provider: String?,
                        status: Int,
                        desc: String?
                    ) {}
                }

                val requestId = tencentLoc.requestSingleFreshLocation(
                    null, listener, Looper.getMainLooper()
                )

                cont.invokeOnCancellation {
                    tencentLoc.removeUpdates(listener)
                }
            } catch (e: Exception) {
                Log.e(TAG, "腾讯定位初始化失败", e)
                cont.resumeWith(Result.success(null))
            }
        }

    // ==================== 系统原生定位 ====================

    @Suppress("MissingPermission")
    private suspend fun locateNative(context: Context): LocationResult? =
        suspendCancellableCoroutine { cont ->
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                cont.resumeWith(Result.success(null))
                return@suspendCancellableCoroutine
            }

            val listener = object : android.location.LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (cont.isCancelled) return
                    try {
                        val (gcjLat, gcjLng) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            // Android 12+ 系统原生返回的是 WGS-84，转换为 GCJ-02
                            CoordinateConverter.wgs84ToGcj02(location.latitude, location.longitude)
                        } else {
                            location.latitude to location.longitude
                        }
                        val result = LocationResult.Success(
                            lat = gcjLat,
                            lng = gcjLng,
                            accuracy = location.accuracy,
                            vendor = "系统原生"
                        )
                        cont.resumeWith(Result.success(result))
                    } catch (e: Exception) {
                        cont.resumeWith(Result.success(null))
                    }
                }

                @Deprecated("legacy")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000L, 0f, listener
                )
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000L, 0f, listener
                )
            } catch (e: SecurityException) {
                Log.e(TAG, "系统原生定位权限不足", e)
                cont.resumeWith(Result.success(null))
                return@suspendCancellableCoroutine
            }

            cont.invokeOnCancellation {
                locationManager.removeUpdates(listener)
            }
        }

    // ==================== 工具 ====================

    private fun isVendorAvailable(vendor: MapVendor): Boolean = when (vendor) {
        MapVendor.AMAP -> MapServiceManager.amapAvailable
        MapVendor.BAIDU -> MapServiceManager.baiduAvailable
        MapVendor.TENCENT -> MapServiceManager.tencentAvailable
    }
}