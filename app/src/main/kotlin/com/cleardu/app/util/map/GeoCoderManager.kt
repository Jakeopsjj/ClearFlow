package com.cleardu.app.util.map

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 地理编码 / 逆地理编码 / 距离计算降级管理器。
 *
 * 降级顺序：高德优先，失败静默切百度，再失败切腾讯。
 *
 * 串行自动静默降级，不给用户弹窗报错、toast 提示。
 * 拿到有效结果立刻终止后续调用；全部服务商失败返回可控业务状态。
 * 禁止并发调用多家接口。
 */
object GeoCoderManager {

    private const val TAG = "GeoCoderManager"
    private const val TIMEOUT_MS = 10_000L

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** 是否正在执行中 */
    private val _executing = MutableStateFlow(false)
    val executing: StateFlow<Boolean> = _executing.asStateFlow()

    /**
     * 地理编码结果。
     */
    data class GeoCodeResult(
        val lat: Double,
        val lng: Double,
        val address: String,
        val vendor: String
    )

    /**
     * 逆地理编码结果。
     */
    data class ReverseGeoCodeResult(
        val address: String,
        val province: String = "",
        val city: String = "",
        val district: String = "",
        val vendor: String
    )

    /**
     * 正向地理编码：地址 → 坐标。
     *
     * @param context Android Context
     * @param address 地址字符串
     * @return 坐标结果，全部失败返回 null
     */
    suspend fun geocode(context: Context, address: String): GeoCodeResult? =
        withContext(Dispatchers.IO) {
            _executing.value = true
            try {
                for (vendor in listOf(MapVendor.AMAP, MapVendor.BAIDU, MapVendor.TENCENT)) {
                    if (!isVendorAvailable(vendor)) continue
                    val result = try {
                        withTimeout(TIMEOUT_MS) {
                            when (vendor) {
                                MapVendor.AMAP -> geocodeAmap(context, address)
                                MapVendor.BAIDU -> geocodeBaidu(address)
                                MapVendor.TENCENT -> geocodeTencent(context, address)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "$vendor 地理编码异常", e)
                        null
                    }
                    if (result != null) {
                        Log.i(TAG, "【数据来源=$vendor】地理编码成功")
                        return@withContext result
                    }
                }
                Log.w(TAG, "所有服务商地理编码均失败")
                null
            } finally {
                _executing.value = false
            }
        }

    /**
     * 逆地理编码：坐标 → 地址。
     *
     * @param context Android Context
     * @param lat 纬度（GCJ-02）
     * @param lng 经度（GCJ-02）
     * @return 地址结果，全部失败返回 null
     */
    suspend fun reverseGeocode(
        context: Context,
        lat: Double,
        lng: Double
    ): ReverseGeoCodeResult? = withContext(Dispatchers.IO) {
        _executing.value = true
        try {
            for (vendor in listOf(MapVendor.AMAP, MapVendor.BAIDU, MapVendor.TENCENT)) {
                if (!isVendorAvailable(vendor)) continue
                val result = try {
                    withTimeout(TIMEOUT_MS) {
                        when (vendor) {
                            MapVendor.AMAP -> reverseGeocodeAmap(context, lat, lng)
                            MapVendor.BAIDU -> reverseGeocodeBaidu(lat, lng)
                            MapVendor.TENCENT -> reverseGeocodeTencent(context, lat, lng)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "$vendor 逆地理编码异常", e)
                    null
                }
                if (result != null) {
                    Log.i(TAG, "【数据来源=$vendor】逆地理编码成功")
                    return@withContext result
                }
            }
            Log.w(TAG, "所有服务商逆地理编码均失败")
            null
        } finally {
            _executing.value = false
        }
    }

    /**
     * 计算两点间距离（Haversine 公式，不依赖 SDK）。
     */
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double =
        CoordinateConverter.calculateDistance(lat1, lng1, lat2, lng2)

    /**
     * 取消当前操作。
     */
    fun cancel() {
        scope.coroutineContext[Job]?.cancelChildren()
        _executing.value = false
    }

    // ==================== 高德实现 ====================

    private suspend fun geocodeAmap(context: Context, address: String): GeoCodeResult? =
        suspendCancellableCoroutine { cont ->
            try {
                val query = com.amap.api.services.geocoder.GeocodeQuery(address, "")
                val geocodeSearch = com.amap.api.services.geocoder.GeocodeSearch(context)
                geocodeSearch.setOnGeocodeSearchListener(object :
                    com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener {
                    override fun onGeocodeSearched(
                        result: com.amap.api.services.geocoder.GeocodeResult?,
                        code: Int
                    ) {
                        if (cont.isCancelled) return
                        try {
                            if (code == 1000 && result != null) {
                                val addr = result.geocodeAddressList?.firstOrNull()
                                if (addr != null && addr.latLonPoint != null) {
                                    cont.resumeWith(
                                        Result.success(
                                            GeoCodeResult(
                                                lat = addr.latLonPoint.latitude,
                                                lng = addr.latLonPoint.longitude,
                                                address = addr.formatAddress ?: "",
                                                vendor = "高德"
                                            )
                                        )
                                    )
                                    return
                                }
                            }
                            cont.resumeWith(Result.success(null))
                        } catch (e: Exception) {
                            cont.resumeWith(Result.success(null))
                        }
                    }

                    override fun onRegeocodeSearched(
                        result: com.amap.api.services.geocoder.RegeocodeResult?,
                        code: Int
                    ) {}
                })
                geocodeSearch.getFromLocationNameAsyn(query)
            } catch (e: Exception) {
                cont.resumeWith(Result.success(null))
            }
        }

    private suspend fun reverseGeocodeAmap(
        context: Context,
        lat: Double,
        lng: Double
    ): ReverseGeoCodeResult? = suspendCancellableCoroutine { cont ->
        try {
            val query = com.amap.api.services.geocoder.RegeocodeQuery(
                com.amap.api.services.core.LatLonPoint(lat, lng), 200f,
                com.amap.api.services.geocoder.GeocodeSearch.AMAP
            )
            val geocodeSearch = com.amap.api.services.geocoder.GeocodeSearch(context)
            geocodeSearch.setOnGeocodeSearchListener(object :
                com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener {
                override fun onRegeocodeSearched(
                    result: com.amap.api.services.geocoder.RegeocodeResult?,
                    code: Int
                ) {
                    if (cont.isCancelled) return
                    try {
                        if (code == 1000 && result != null) {
                            val addr = result.regeocodeAddress
                            cont.resumeWith(
                                Result.success(
                                    ReverseGeoCodeResult(
                                        address = addr?.formatAddress ?: "",
                                        province = addr?.province ?: "",
                                        city = addr?.city ?: "",
                                        district = addr?.district ?: "",
                                        vendor = "高德"
                                    )
                                )
                            )
                            return
                        }
                        cont.resumeWith(Result.success(null))
                    } catch (e: Exception) {
                        cont.resumeWith(Result.success(null))
                    }
                }

                override fun onGeocodeSearched(
                    result: com.amap.api.services.geocoder.GeocodeResult?,
                    code: Int
                ) {}
            })
            geocodeSearch.getFromLocationAsyn(query)
        } catch (e: Exception) {
            cont.resumeWith(Result.success(null))
        }
    }

    // ==================== 百度实现 ====================

    private suspend fun geocodeBaidu(address: String): GeoCodeResult? =
        suspendCancellableCoroutine { cont ->
            try {
                val geoCoder = com.baidu.mapapi.search.geocode.GeoCoder.newInstance()
                geoCoder.setOnGetGeoCodeResultListener(object :
                    com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener {
                    override fun onGetGeoCodeResult(
                        result: com.baidu.mapapi.search.geocode.GeoCodeResult?
                    ) {
                        if (cont.isCancelled) return
                        try {
                            if (result != null && result.location != null) {
                                val (gcjLat, gcjLng) = CoordinateConverter.bd09ToGcj02(
                                    result.location.latitude,
                                    result.location.longitude
                                )
                                cont.resumeWith(
                                    Result.success(
                                        GeoCodeResult(
                                            lat = gcjLat,
                                            lng = gcjLng,
                                            address = result.address ?: "",
                                            vendor = "百度"
                                        )
                                    )
                                )
                                return
                            }
                            cont.resumeWith(Result.success(null))
                        } catch (e: Exception) {
                            cont.resumeWith(Result.success(null))
                        }
                    }

                    override fun onGetReverseGeoCodeResult(
                        result: com.baidu.mapapi.search.geocode.ReverseGeoCodeResult?
                    ) {}
                })
                geoCoder.geocode(
                    com.baidu.mapapi.search.geocode.GeoCodeOption()
                        .city("")
                        .address(address)
                )
                cont.invokeOnCancellation { geoCoder.destroy() }
            } catch (e: Exception) {
                cont.resumeWith(Result.success(null))
            }
        }

    private suspend fun reverseGeocodeBaidu(
        lat: Double,
        lng: Double
    ): ReverseGeoCodeResult? = suspendCancellableCoroutine { cont ->
        try {
            val (bdLat, bdLng) = CoordinateConverter.gcj02ToBd09(lat, lng)
            val geoCoder = com.baidu.mapapi.search.geocode.GeoCoder.newInstance()
            geoCoder.setOnGetGeoCodeResultListener(object :
                com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener {
                override fun onGetReverseGeoCodeResult(
                    result: com.baidu.mapapi.search.geocode.ReverseGeoCodeResult?
                ) {
                    if (cont.isCancelled) return
                    try {
                        if (result != null) {
                            cont.resumeWith(
                                Result.success(
                                    ReverseGeoCodeResult(
                                        address = result.address ?: "",
                                        province = result.addressDetail?.province ?: "",
                                        city = result.addressDetail?.city ?: "",
                                        district = result.addressDetail?.district ?: "",
                                        vendor = "百度"
                                    )
                                )
                            )
                            return
                        }
                        cont.resumeWith(Result.success(null))
                    } catch (e: Exception) {
                        cont.resumeWith(Result.success(null))
                    }
                }

                override fun onGetGeoCodeResult(
                    result: com.baidu.mapapi.search.geocode.GeoCodeResult?
                ) {}
            })
            geoCoder.reverseGeoCode(
                com.baidu.mapapi.search.geocode.ReverseGeoCodeOption()
                    .location(com.baidu.mapapi.model.LatLng(bdLat, bdLng))
            )
            cont.invokeOnCancellation { geoCoder.destroy() }
        } catch (e: Exception) {
            cont.resumeWith(Result.success(null))
        }
    }

    // ==================== 腾讯实现 ====================
    // TODO: 腾讯 SDK 5.4.1 的 sdk-utilities 1.0.9 中不包含 GeocoderSearch 类，
    // 需要通过腾讯地图 WebService API 或其他方式实现地理编码。

    private suspend fun geocodeTencent(context: Context, address: String): GeoCodeResult? {
        Log.w(TAG, "腾讯地理编码暂不可用（SDK 不支持）")
        return null
    }

    private suspend fun reverseGeocodeTencent(
        context: Context,
        lat: Double,
        lng: Double
    ): ReverseGeoCodeResult? {
        Log.w(TAG, "腾讯逆地理编码暂不可用（SDK 不支持）")
        return null
    }

    // ==================== 工具 ====================

    private fun isVendorAvailable(vendor: MapVendor): Boolean = when (vendor) {
        MapVendor.AMAP -> MapServiceManager.amapAvailable
        MapVendor.BAIDU -> MapServiceManager.baiduAvailable
        MapVendor.TENCENT -> MapServiceManager.tencentAvailable
    }
}