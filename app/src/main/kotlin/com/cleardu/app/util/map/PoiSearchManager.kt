package com.cleardu.app.util.map

import android.content.Context
import android.util.Log
import com.cleardu.app.ui.components.NearbyHospital
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * POI 搜索降级管理器。
 *
 * 降级顺序：百度 POI → 高德 POI → 腾讯 POI。
 *
 * 串行自动静默降级，不给用户弹窗报错、toast 提示。
 * 拿到有效结果立刻终止后续调用；全部服务商失败返回可控业务状态。
 * 禁止并发调用多家接口。
 *
 * 页面销毁立刻中断全部异步 POI 请求。
 */
object PoiSearchManager {

    private const val TAG = "PoiSearchManager"
    private const val SEARCH_TIMEOUT_MS = 10_000L
    private const val SEARCH_RADIUS_METERS = 50_000  // 50km 搜索半径

    /** 搜索 Job，用于取消 */
    @Volatile
    private var searchJob: Job? = null

    /** 是否正在搜索中 */
    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    /**
     * 搜索附近医院。
     *
     * @param context Android Context
     * @param lat 纬度（GCJ-02）
     * @param lng 经度（GCJ-02）
     * @param radius 搜索半径（米）
     * @return 搜索结果（Pair<服务商, 医院列表>），全部失败返回空列表
     */
    suspend fun searchNearbyHospitals(
        context: Context,
        lat: Double,
        lng: Double,
        radius: Int = SEARCH_RADIUS_METERS
    ): List<NearbyHospital> = withContext(Dispatchers.IO) {
        Log.i(TAG, "===== 开始搜索附近医院: lat=$lat, lng=$lng, radius=$radius =====")
        _searching.value = true

        try {
            // 降级顺序：百度 → 高德 → 腾讯
            val priority = listOf(MapVendor.BAIDU, MapVendor.AMAP, MapVendor.TENCENT)

            for (vendor in priority) {
                if (!isVendorAvailable(vendor)) {
                    Log.d(TAG, "$vendor 不可用，跳过")
                    continue
                }

                Log.i(TAG, "尝试 $vendor POI 搜索...")
                val results = try {
                    withTimeout(SEARCH_TIMEOUT_MS) {
                        when (vendor) {
                            MapVendor.BAIDU -> searchBaidu(context, lat, lng, radius)
                            MapVendor.AMAP -> searchAmap(context, lat, lng, radius)
                            MapVendor.TENCENT -> searchTencent(context, lat, lng, radius)
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.e(TAG, "$vendor POI 搜索超时")
                    null
                } catch (e: CancellationException) {
                    Log.w(TAG, "$vendor POI 搜索被取消")
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "$vendor POI 搜索异常", e)
                    null
                }

                if (results != null && results.isNotEmpty()) {
                    Log.i(TAG, "【数据来源=$vendor】搜索到 ${results.size} 家医院，终止后续调用")
                    return@withContext results
                }
            }

            Log.w(TAG, "所有服务商 POI 搜索均无结果")
            emptyList()
        } finally {
            _searching.value = false
        }
    }

    /**
     * 取消当前搜索。
     */
    fun cancel() {
        searchJob?.cancel()
        searchJob = null
        _searching.value = false
        Log.i(TAG, "POI 搜索已取消")
    }

    // ==================== 百度 POI 搜索 ====================

    private suspend fun searchBaidu(
        context: Context,
        lat: Double,
        lng: Double,
        radius: Int
    ): List<NearbyHospital>? = suspendCancellableCoroutine { cont ->
        try {
            val (bdLat, bdLng) = CoordinateConverter.gcj02ToBd09(lat, lng)

            val poiSearch = com.baidu.mapapi.search.poi.PoiSearch.newInstance()
            poiSearch.setOnGetPoiSearchResultListener(object :
                com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener {
                override fun onGetPoiResult(result: com.baidu.mapapi.search.poi.PoiResult?) {
                    if (cont.isCancelled) return
                    try {
                        val pois = result?.allPoi
                        if (pois == null || pois.isEmpty()) {
                            cont.resumeWith(Result.success(null))
                            return
                        }
                        val hospitals = pois.mapNotNull { poi ->
                            val (gcjLat, gcjLng) = CoordinateConverter.bd09ToGcj02(
                                poi.location?.latitude ?: return@mapNotNull null,
                                poi.location?.longitude ?: return@mapNotNull null
                            )
                            NearbyHospital(
                                name = poi.name ?: "",
                                address = poi.address ?: "",
                                distance = formatDistance(
                                    CoordinateConverter.calculateDistance(
                                        lat, lng, gcjLat, gcjLng
                                    )
                                ),
                                lat = gcjLat,
                                lng = gcjLng
                            )
                        }.sortedBy {
                            CoordinateConverter.calculateDistance(lat, lng, it.lat, it.lng)
                        }
                        cont.resumeWith(Result.success(hospitals.take(30)))
                    } catch (e: Exception) {
                        cont.resumeWith(Result.success(null))
                    }
                }

                override fun onGetPoiDetailResult(
                    result: com.baidu.mapapi.search.poi.PoiDetailResult?
                ) {}

                override fun onGetPoiDetailResult(
                    result: com.baidu.mapapi.search.poi.PoiDetailSearchResult?
                ) {}

                override fun onGetPoiIndoorResult(
                    result: com.baidu.mapapi.search.poi.PoiIndoorResult?
                ) {}
            })

            val option = com.baidu.mapapi.search.poi.PoiNearbySearchOption()
                .keyword("透析医院")
                .location(com.baidu.mapapi.model.LatLng(bdLat, bdLng))
                .radius(radius)
                .pageNum(0)
                .pageCapacity(30)

            poiSearch.searchNearby(option)

            cont.invokeOnCancellation {
                poiSearch.destroy()
            }
        } catch (e: Exception) {
            Log.e(TAG, "百度 POI 搜索初始化失败", e)
            cont.resumeWith(Result.success(null))
        }
    }

    // ==================== 高德 POI 搜索 ====================

    private suspend fun searchAmap(
        context: Context,
        lat: Double,
        lng: Double,
        radius: Int
    ): List<NearbyHospital>? = suspendCancellableCoroutine { cont ->
        try {
            val query = com.amap.api.services.poisearch.PoiSearch.Query("透析医院", "", "")
            query.pageSize = 30
            query.pageNum = 0

            val poiSearch = com.amap.api.services.poisearch.PoiSearch(context, query)
            poiSearch.setBound(
                com.amap.api.services.poisearch.PoiSearch.SearchBound(
                    com.amap.api.services.core.LatLonPoint(lat, lng),
                    radius
                )
            )

            poiSearch.setOnPoiSearchListener(object :
                com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener {
                override fun onPoiSearched(
                    result: com.amap.api.services.poisearch.PoiResult?,
                    code: Int
                ) {
                    if (cont.isCancelled) return
                    try {
                        if (code != 1000 || result == null) {
                            cont.resumeWith(Result.success(null))
                            return
                        }
                        val pois = result.pois
                        if (pois == null || pois.isEmpty()) {
                            cont.resumeWith(Result.success(null))
                            return
                        }
                        val hospitals = pois.mapNotNull { poi ->
                            NearbyHospital(
                                name = poi.title ?: "",
                                address = poi.snippet ?: "",
                                distance = formatDistance(
                                    CoordinateConverter.calculateDistance(
                                        lat, lng,
                                        poi.latLonPoint?.latitude ?: return@mapNotNull null,
                                        poi.latLonPoint?.longitude ?: return@mapNotNull null
                                    )
                                ),
                                lat = poi.latLonPoint.latitude,
                                lng = poi.latLonPoint.longitude
                            )
                        }.sortedBy {
                            CoordinateConverter.calculateDistance(lat, lng, it.lat, it.lng)
                        }
                        cont.resumeWith(Result.success(hospitals))
                    } catch (e: Exception) {
                        cont.resumeWith(Result.success(null))
                    }
                }

                override fun onPoiItemSearched(
                    poi: com.amap.api.services.core.PoiItem?,
                    code: Int
                ) {}
            })

            poiSearch.searchPOIAsyn()

            cont.invokeOnCancellation {
                // 高德 PoiSearch 没有显式 destroy，但可以置空
            }
        } catch (e: Exception) {
            Log.e(TAG, "高德 POI 搜索初始化失败", e)
            cont.resumeWith(Result.success(null))
        }
    }

    // ==================== 腾讯 POI 搜索 ====================
    // TODO: 腾讯 SDK 5.4.1 的 sdk-utilities 1.0.9 中不包含 PoiSearch 类，
    // 需要通过腾讯地图 WebService API 或其他方式实现 POI 搜索。

    private suspend fun searchTencent(
        context: Context,
        lat: Double,
        lng: Double,
        radius: Int
    ): List<NearbyHospital>? {
        Log.w(TAG, "腾讯 POI 搜索暂不可用（SDK 不支持）")
        return null
    }

    // ==================== 工具 ====================

    private fun isVendorAvailable(vendor: MapVendor): Boolean = when (vendor) {
        MapVendor.AMAP -> MapServiceManager.amapAvailable
        MapVendor.BAIDU -> MapServiceManager.baiduAvailable
        MapVendor.TENCENT -> MapServiceManager.tencentAvailable
    }

    private fun formatDistance(meters: Double): String = when {
        meters < 1000 -> "${meters.toInt()}m"
        else -> "${"%.1f".format(meters / 1000)}km"
    }
}