package com.cleardu.app.util.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.cleardu.app.R

/**
 * 统一封装三家地图 SDK 的 MapView，提供一致的接口。
 *
 * 每个 MapViewWrapper 持有对应厂商的 MapView 实例，
 * 负责生命周期管理、地图中心/缩放设置、Marker 添加/清除。
 */
sealed class MapViewWrapper {

    abstract val view: View
    abstract val vendor: MapVendor

    abstract fun onCreate(savedInstanceState: android.os.Bundle?)
    abstract fun onResume()
    abstract fun onPause()
    abstract fun onDestroy()

    abstract fun setCenter(lat: Double, lng: Double, zoom: Float)
    abstract fun getCenter(): Pair<Double, Double>
    abstract fun getZoom(): Float

    abstract fun addMarker(
        lat: Double, lng: Double,
        title: String, snippet: String,
        icon: Bitmap?,
        onClick: (() -> Unit)?
    ): Any

    abstract fun clearMarkers()

    abstract fun setOnMapLoadedCallback(callback: () -> Unit)

    // ==================== 高德实现 ====================

    class AmapWrapper(context: Context) : MapViewWrapper() {
        override val vendor = MapVendor.AMAP
        override val view: com.amap.api.maps.MapView = com.amap.api.maps.MapView(context)
        private var aMap: com.amap.api.maps.AMap? = null
        private val markers = mutableListOf<com.amap.api.maps.model.Marker>()

        init {
            view.onCreate(null)
        }

        override fun onCreate(savedInstanceState: android.os.Bundle?) {
            view.onCreate(savedInstanceState)
            aMap = view.map
            aMap?.apply {
                uiSettings.isZoomControlsEnabled = false
                uiSettings.isMyLocationButtonEnabled = false
            }
        }

        override fun onResume() { view.onResume() }
        override fun onPause() { view.onPause() }
        override fun onDestroy() {
            clearMarkers()
            aMap = null
            view.onDestroy()
        }

        override fun setCenter(lat: Double, lng: Double, zoom: Float) {
            aMap?.moveCamera(
                com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(
                    com.amap.api.maps.model.LatLng(lat, lng), zoom
                )
            )
        }

        override fun getCenter(): Pair<Double, Double> {
            val target = aMap?.cameraPosition?.target ?: return 0.0 to 0.0
            return target.latitude to target.longitude
        }

        override fun getZoom(): Float = aMap?.cameraPosition?.zoom ?: 14f

        override fun addMarker(
            lat: Double, lng: Double,
            title: String, snippet: String,
            icon: Bitmap?,
            onClick: (() -> Unit)?
        ): Any {
            val options = com.amap.api.maps.model.MarkerOptions()
                .position(com.amap.api.maps.model.LatLng(lat, lng))
                .title(title)
                .snippet(snippet)
            if (icon != null) {
                options.icon(com.amap.api.maps.model.BitmapDescriptorFactory.fromBitmap(icon))
            }
            val marker = aMap?.addMarker(options)
            if (marker != null) {
                markers.add(marker)
                if (onClick != null) {
                    aMap?.setOnMarkerClickListener { m ->
                        if (m.id == marker.id) { onClick(); true } else false
                    }
                }
            }
            return marker ?: Unit
        }

        override fun clearMarkers() {
            markers.forEach { it.remove() }
            markers.clear()
        }

        override fun setOnMapLoadedCallback(callback: () -> Unit) {
            aMap?.setOnMapLoadedListener(callback)
        }
    }

    // ==================== 百度实现 ====================

    class BaiduWrapper(context: Context) : MapViewWrapper() {
        override val vendor = MapVendor.BAIDU
        override val view: com.baidu.mapapi.map.MapView =
            com.baidu.mapapi.map.MapView(context)
        private var baiduMap: com.baidu.mapapi.map.BaiduMap? = null
        private val markers = mutableListOf<com.baidu.mapapi.map.Marker>()

        override fun onCreate(savedInstanceState: android.os.Bundle?) {
            baiduMap = view.map
            baiduMap?.apply {
                uiSettings.isZoomGesturesEnabled = true
                uiSettings.isScrollGesturesEnabled = true
            }
        }

        override fun onResume() { view.onResume() }
        override fun onPause() { view.onPause() }
        override fun onDestroy() {
            clearMarkers()
            baiduMap = null
            view.onDestroy()
        }

        override fun setCenter(lat: Double, lng: Double, zoom: Float) {
            val (bdLat, bdLng) = CoordinateConverter.gcj02ToBd09(lat, lng)
            baiduMap?.animateMapStatus(
                com.baidu.mapapi.map.MapStatusUpdateFactory.newLatLngZoom(
                    com.baidu.mapapi.model.LatLng(bdLat, bdLng), zoom
                )
            )
        }

        override fun getCenter(): Pair<Double, Double> {
            val target = baiduMap?.mapStatus?.target ?: return 0.0 to 0.0
            return CoordinateConverter.bd09ToGcj02(target.latitude, target.longitude)
        }

        override fun getZoom(): Float = (baiduMap?.mapStatus?.zoom ?: 14).toFloat()

        override fun addMarker(
            lat: Double, lng: Double,
            title: String, snippet: String,
            icon: Bitmap?,
            onClick: (() -> Unit)?
        ): Any {
            val (bdLat, bdLng) = CoordinateConverter.gcj02ToBd09(lat, lng)
            val options = com.baidu.mapapi.map.MarkerOptions()
                .position(com.baidu.mapapi.model.LatLng(bdLat, bdLng))
                .title(title)
            if (icon != null) {
                options.icon(com.baidu.mapapi.map.BitmapDescriptorFactory.fromBitmap(icon))
            }
            val marker = baiduMap?.addOverlay(options) as? com.baidu.mapapi.map.Marker
            if (marker != null) {
                markers.add(marker)
                if (onClick != null) {
                    baiduMap?.setOnMarkerClickListener { m ->
                        if (m === marker) { onClick(); true } else false
                    }
                }
            }
            return marker ?: Unit
        }

        override fun clearMarkers() {
            markers.forEach { it.remove() }
            markers.clear()
        }

        override fun setOnMapLoadedCallback(callback: () -> Unit) {
            baiduMap?.setOnMapLoadedCallback { callback() }
        }
    }

    // ==================== 腾讯实现 ====================

    class TencentWrapper(context: Context) : MapViewWrapper() {
        override val vendor = MapVendor.TENCENT
        override val view: com.tencent.tencentmap.mapsdk.maps.MapView =
            com.tencent.tencentmap.mapsdk.maps.MapView(context)
        private var tencentMap: com.tencent.tencentmap.mapsdk.maps.TencentMap? = null
        private val markers = mutableListOf<com.tencent.tencentmap.mapsdk.maps.model.Marker>()

        override fun onCreate(savedInstanceState: android.os.Bundle?) {
            tencentMap = view.map
            tencentMap?.apply {
                uiSettings.isZoomGesturesEnabled = true
                uiSettings.isScrollGesturesEnabled = true
            }
        }

        override fun onResume() { view.onResume() }
        override fun onPause() { view.onPause() }
        override fun onDestroy() {
            clearMarkers()
            tencentMap = null
            view.onDestroy()
        }

        override fun setCenter(lat: Double, lng: Double, zoom: Float) {
            tencentMap?.moveCamera(
                com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory.newLatLngZoom(
                    com.tencent.tencentmap.mapsdk.maps.model.LatLng(lat, lng), zoom
                )
            )
        }

        override fun getCenter(): Pair<Double, Double> {
            val target = tencentMap?.cameraPosition?.target ?: return 0.0 to 0.0
            return target.latitude to target.longitude
        }

        override fun getZoom(): Float = (tencentMap?.cameraPosition?.zoom ?: 14.0).toFloat()

        override fun addMarker(
            lat: Double, lng: Double,
            title: String, snippet: String,
            icon: Bitmap?,
            onClick: (() -> Unit)?
        ): Any {
            val options = com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions(
                com.tencent.tencentmap.mapsdk.maps.model.LatLng(lat, lng)
            ).title(title).snippet(snippet)
            if (icon != null) {
                options.icon(com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory.fromBitmap(icon))
            }
            val marker = tencentMap?.addMarker(options)
            if (marker != null) {
                markers.add(marker)
                if (onClick != null) {
                    tencentMap?.setOnMarkerClickListener { m ->
                        if (m === marker) { onClick(); true } else false
                    }
                }
            }
            return marker ?: Unit
        }

        override fun clearMarkers() {
            markers.forEach { it.remove() }
            markers.clear()
        }

        override fun setOnMapLoadedCallback(callback: () -> Unit) {
            tencentMap?.setOnMapLoadedCallback { callback() }
        }
    }

    companion object {
        private const val TAG = "MapViewWrapper"

        fun create(context: Context, vendor: MapVendor): MapViewWrapper? {
            return try {
                when (vendor) {
                    MapVendor.AMAP -> AmapWrapper(context)
                    MapVendor.BAIDU -> BaiduWrapper(context)
                    MapVendor.TENCENT -> TencentWrapper(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "创建 ${vendor} MapView 失败", e)
                null
            }
        }
    }
}