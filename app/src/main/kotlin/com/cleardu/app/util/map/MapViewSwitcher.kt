package com.cleardu.app.util.map

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 地图视图切换迁移管理器。
 *
 * 降级顺序：高德 MapView → 百度 MapView → 腾讯 MapView。
 *
 * 优先在 SDK 初始化阶段检测地图可用性；
 * 仅当正在渲染的地图发生运行时致命异常，才允许现场切换视图。
 *
 * 切换流程：
 * 1. 标记 transitioning=true，显示透明蒙层
 * 2. 销毁旧视图（onDestroy + 清空监听 + 清空 marker + 释放资源）
 * 3. 创建新视图
 * 4. 迁移地图中心点、缩放级别、全部 Marker 点位
 * 5. 新地图渲染完毕后移除蒙层
 *
 * 防崩溃：
 * - 每家 SDK 初始化独立 try-catch，单家异常只标记不可用
 * - 切换 MapView 必须完整执行 onDestroy
 * - 页面销毁立刻中断全部异步操作
 * - 三家全部失效展示友好空白
 * - 后台/页面销毁状态禁止执行切换
 */
class MapViewSwitcher(private val context: Context) {

    companion object {
        private const val TAG = "MapViewSwitcher"
        private const val TRANSITION_DELAY_MS = 300L
    }

    /** 当前正在渲染的地图服务商 */
    private val _currentVendor = MutableStateFlow<MapVendor?>(null)
    val currentVendor: StateFlow<MapVendor?> = _currentVendor.asStateFlow()

    /** 是否正在切换中（用于显示透明蒙层） */
    private val _transitioning = MutableStateFlow(false)
    val transitioning: StateFlow<Boolean> = _transitioning.asStateFlow()

    /** 当前活跃的 MapView 包装器 */
    @Volatile
    var currentWrapper: MapViewWrapper? = null
        private set

    /** 已尝试过的服务商列表（用于切换时不重复尝试） */
    private val attemptedVendors = mutableSetOf<MapVendor>()

    /** 页面是否已销毁 */
    @Volatile
    private var destroyed = false

    /** 切换协程作用域 */
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 保存的地图状态（用于迁移）
    private var savedCenterLat: Double = 0.0
    private var savedCenterLng: Double = 0.0
    private var savedZoom: Float = 14f

    data class MarkerState(
        val lat: Double,
        val lng: Double,
        val title: String,
        val snippet: String
    )

    private val savedMarkers = mutableListOf<MarkerState>()

    /** 地图加载完成回调 */
    private var onMapLoaded: (() -> Unit)? = null

    /** 标记点击回调 */
    private var markerClickCallback: ((Int) -> Unit)? = null

    /**
     * 初始化：选择第一个可用的服务商并创建 MapView。
     *
     * @return 是否成功创建地图视图
     */
    fun initialize(): Boolean {
        if (destroyed) {
            Log.w(TAG, "页面已销毁，跳过初始化")
            return false
        }

        val priority = MapServiceManager.mapViewPriority()
        if (priority.isEmpty()) {
            Log.e(TAG, "所有地图 SDK 均不可用，无法初始化地图视图")
            return false
        }

        val vendor = priority.first()
        Log.i(TAG, "初始化地图视图，首选服务商: $vendor")

        return createAndSetWrapper(vendor)
    }

    /**
     * 创建指定服务商的 MapView 并设为当前活跃视图。
     */
    private fun createAndSetWrapper(vendor: MapVendor): Boolean {
        return try {
            val wrapper = MapViewWrapper.create(context, vendor)
            if (wrapper == null) {
                Log.e(TAG, "创建 $vendor MapView 失败（wrapper 为 null）")
                attemptedVendors.add(vendor)
                return false
            }

            wrapper.onCreate(null)
            wrapper.setCenter(savedCenterLat, savedCenterLng, savedZoom)

            // 迁移已保存的 Marker
            for (marker in savedMarkers) {
                wrapper.addMarker(
                    marker.lat, marker.lng,
                    marker.title, marker.snippet,
                    null
                ) { markerClickCallback?.invoke(savedMarkers.indexOf(marker)) }
            }

            wrapper.setOnMapLoadedCallback {
                Log.i(TAG, "$vendor 地图加载完成")
                onMapLoaded?.invoke()
            }

            currentWrapper = wrapper
            _currentVendor.value = vendor
            attemptedVendors.add(vendor)
            Log.i(TAG, "成功创建并激活 $vendor MapView")
            true
        } catch (e: Exception) {
            Log.e(TAG, "创建 $vendor MapView 异常", e)
            attemptedVendors.add(vendor)
            false
        }
    }

    /**
     * 处理运行时致命异常，自动切换到下一个可用服务商。
     *
     * 仅在页面未销毁且当前正在前台运行时执行切换。
     */
    fun onFatalError(lat: Double, lng: Double, zoom: Float) {
        if (destroyed) {
            Log.w(TAG, "页面已销毁，禁止执行地图服务商切换")
            return
        }

        val current = _currentVendor.value ?: return
        Log.e(TAG, "===== $current 发生致命异常，开始切换服务商 =====")

        // 保存当前状态
        savedCenterLat = lat
        savedCenterLng = lng
        savedZoom = zoom

        scope.launch {
            switchToNextVendor()
        }
    }

    /**
     * 切换到下一个可用服务商。
     */
    private suspend fun switchToNextVendor() {
        _transitioning.value = true

        // 短暂延迟让蒙层先显示
        delay(TRANSITION_DELAY_MS)

        try {
            // 1. 销毁旧视图
            destroyCurrentWrapper()

            // 2. 查找下一个可用服务商
            val priority = MapServiceManager.mapViewPriority()
            val nextVendor = priority.firstOrNull { it !in attemptedVendors }

            if (nextVendor == null) {
                Log.e(TAG, "所有可用服务商均已尝试失败，无更多服务商可切换")
                _currentVendor.value = null
                currentWrapper = null
                _transitioning.value = false
                return
            }

            Log.i(TAG, "切换到下一个服务商: $nextVendor")

            // 3. 创建新视图
            val success = createAndSetWrapper(nextVendor)

            if (!success) {
                // 当前服务商创建失败，递归尝试下一个
                Log.w(TAG, "$nextVendor 创建失败，尝试下一个")
                switchToNextVendor()
                return
            }

            // 4. 重置已尝试列表（为下次切换准备）
            attemptedVendors.clear()
            attemptedVendors.add(nextVendor)

            Log.i(TAG, "===== 切换完成，当前服务商: $nextVendor =====")
        } catch (e: Exception) {
            Log.e(TAG, "切换服务商过程中异常", e)
        } finally {
            // 延迟确保新地图渲染后再移除蒙层
            delay(500L)
            _transitioning.value = false
        }
    }

    /**
     * 销毁当前活跃的 MapView 包装器，释放全部资源。
     */
    private fun destroyCurrentWrapper() {
        val wrapper = currentWrapper ?: return
        Log.i(TAG, "销毁当前 ${wrapper.vendor} MapView")

        try {
            wrapper.clearMarkers()
            wrapper.onPause()
            wrapper.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "销毁 MapView 时异常", e)
        } finally {
            currentWrapper = null
        }
    }

    // ==================== 公开 API ====================

    /**
     * 设置地图中心点和缩放级别。
     */
    fun setCenter(lat: Double, lng: Double, zoom: Float) {
        savedCenterLat = lat
        savedCenterLng = lng
        savedZoom = zoom
        currentWrapper?.setCenter(lat, lng, zoom)
    }

    /**
     * 获取当前地图中心点（GCJ-02 坐标系）。
     */
    fun getCenter(): Pair<Double, Double> {
        return currentWrapper?.getCenter() ?: (savedCenterLat to savedCenterLng)
    }

    /**
     * 获取当前缩放级别。
     */
    fun getZoom(): Float {
        return currentWrapper?.getZoom() ?: savedZoom
    }

    /**
     * 添加 Marker。
     *
     * @param lat 纬度（GCJ-02）
     * @param lng 经度（GCJ-02）
     * @param title 标题
     * @param snippet 描述
     * @param icon Marker 图标
     * @param onClick 点击回调
     */
    fun addMarker(
        lat: Double, lng: Double,
        title: String, snippet: String,
        icon: Bitmap? = null,
        onClick: (() -> Unit)? = null
    ) {
        savedMarkers.add(MarkerState(lat, lng, title, snippet))
        currentWrapper?.addMarker(lat, lng, title, snippet, icon, onClick)
    }

    /**
     * 清除所有 Marker。
     */
    fun clearMarkers() {
        savedMarkers.clear()
        currentWrapper?.clearMarkers()
    }

    /**
     * 批量添加 Marker（替换现有 Marker）。
     */
    fun setMarkers(
        markers: List<MarkerState>,
        icon: Bitmap? = null,
        onMarkerClick: ((Int) -> Unit)? = null
    ) {
        savedMarkers.clear()
        savedMarkers.addAll(markers)
        currentWrapper?.clearMarkers()
        markerClickCallback = onMarkerClick

        markers.forEachIndexed { index, marker ->
            currentWrapper?.addMarker(
                marker.lat, marker.lng,
                marker.title, marker.snippet,
                icon
            ) { onMarkerClick?.invoke(index) }
        }
    }

    /**
     * 设置地图加载完成回调。
     */
    fun setOnMapLoadedCallback(callback: () -> Unit) {
        onMapLoaded = callback
    }

    /**
     * 生命周期：onResume。
     */
    fun onResume() {
        currentWrapper?.onResume()
    }

    /**
     * 生命周期：onPause。
     */
    fun onPause() {
        currentWrapper?.onPause()
    }

    /**
     * 生命周期：onDestroy。
     * 页面销毁时调用，彻底释放资源。
     */
    fun onDestroy() {
        Log.i(TAG, "MapViewSwitcher.onDestroy()")
        destroyed = true
        scope.cancel()
        destroyCurrentWrapper()
        savedMarkers.clear()
        attemptedVendors.clear()
    }
}