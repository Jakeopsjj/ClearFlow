package com.cleardu.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.util.map.MapServiceManager
import com.cleardu.app.util.map.MapViewSwitcher
import com.cleardu.app.util.map.MapVendor

/**
 * 地图视图组件，使用 MapViewSwitcher 实现三家 SDK 自动降级切换。
 *
 * 降级顺序：高德 MapView → 百度 MapView → 腾讯 MapView。
 *
 * 切换时完整销毁旧视图释放全部资源，增加透明过渡蒙层掩盖闪烁，
 * 新地图渲染完毕再移除蒙层；
 * 自动迁移地图中心点、缩放级别、全部 Marker 点位。
 *
 * 三家全部失效时展示友好空白提示。
 *
 * @param userLat 用户纬度（GCJ-02）
 * @param userLng 用户经度（GCJ-02）
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

    // 创建 MapViewSwitcher 实例（remember 确保重组时不重建）
    val switcher = remember {
        MapViewSwitcher(context).apply {
            val ok = initialize()
            if (ok) {
                setCenter(userLat, userLng, 14f)
            }
        }
    }

    // 生命周期管理
    DisposableEffect(Unit) {
        switcher.onResume()
        onDispose {
            switcher.onPause()
            switcher.onDestroy()
        }
    }

    // 当前服务商状态
    val currentVendor by switcher.currentVendor.collectAsState()
    val transitioning by switcher.transitioning.collectAsState()

    // 更新医院标记
    LaunchedEffect(hospitals) {
        if (currentVendor == null) return@LaunchedEffect

        switcher.clearMarkers()
        val markerStates = hospitals.map { hospital ->
            MapViewSwitcher.MarkerState(
                lat = hospital.lat,
                lng = hospital.lng,
                title = hospital.name,
                snippet = "${hospital.distance}  ${hospital.address}"
            )
        }
        switcher.setMarkers(markerStates) { index ->
            if (index < hospitals.size) {
                onHospitalClick(hospitals[index])
            }
        }
    }

    // 更新地图中心
    LaunchedEffect(userLat, userLng) {
        if (currentVendor != null) {
            switcher.setCenter(userLat, userLng, switcher.getZoom())
        }
    }

    if (currentVendor == null && !MapServiceManager.anyAvailable()) {
        // 三家全部失效，展示友好空白
        Box(
            modifier = modifier.background(
                androidx.compose.ui.graphics.Color(LiquidGlassColors.GlassBg.toArgb())
            ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text(
                "地图服务暂不可用",
                style = com.cleardu.app.ui.theme.ClearDuTypography.MedDetail,
                color = LiquidGlassColors.Text400
            )
        }
        return
    }

    Box(modifier = modifier) {
        // 使用 key 根据 vendor 切换，强制重建 AndroidView
        key(currentVendor) {
            val wrapper = switcher.currentWrapper
            if (wrapper != null) {
                AndroidView(
                    factory = { wrapper.view },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 切换过渡蒙层
        if (transitioning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)
                    )
            )
        }
    }
}