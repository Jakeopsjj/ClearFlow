package com.cleardu.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay

/**
 * 基于 OSMDroid 的地图组件，展示用户位置和附近医院标记。
 *
 * @param userLat 用户纬度
 * @param userLng 用户经度
 * @param hospitals 附近医院列表
 * @param onHospitalClick 点击医院标记回调
 * @param modifier 修饰符
 */
@Composable
fun HospitalMapView(
    userLat: Double,
    userLng: Double,
    hospitals: List<NearbyHospital>,
    onHospitalClick: (NearbyHospital) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)

            // 设置地图中心为用户位置
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(userLat, userLng))

            // 添加指南针
            overlays.add(CompassOverlay(context, this))
        }
    }

    // 生命周期管理
    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // 更新医院标记
    DisposableEffect(hospitals) {
        // 清除旧的医院标记（保留非 Marker 的 overlay）
        val toRemove = mapView.overlays.filterIsInstance<Marker>()
            .filter { it.id != "user_location" }
        mapView.overlays.removeAll(toRemove.toSet())

        // 添加医院标记
        for (hospital in hospitals) {
            val marker = Marker(mapView).apply {
                position = GeoPoint(hospital.lat, hospital.lng)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = hospital.name
                snippet = "${hospital.distance}  ${hospital.address}"
                // 使用系统默认标记图标
                setOnMarkerClickListener { _, _ ->
                    onHospitalClick(hospital)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
        onDispose { }
    }

    // 更新地图中心
    DisposableEffect(userLat, userLng) {
        mapView.controller.setCenter(GeoPoint(userLat, userLng))
        onDispose { }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}