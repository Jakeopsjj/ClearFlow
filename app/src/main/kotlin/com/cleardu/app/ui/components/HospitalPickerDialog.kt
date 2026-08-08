package com.cleardu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cleardu.app.data.AppSettings
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors
import com.cleardu.app.util.LocationHelper
import com.cleardu.app.util.map.LocationFallbackManager
import com.cleardu.app.util.map.PoiSearchManager
import kotlinx.coroutines.launch

/**
 * Nearby hospital data for selection.
 */
data class NearbyHospital(
    val name: String,
    val address: String,
    val distance: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

/** 用户当前位置状态 */
private sealed class UserLocationState {
    data object Loading : UserLocationState()
    data class Success(val lat: Double, val lng: Double, val accuracy: Float, val vendor: String) : UserLocationState()
    data class Error(val message: String) : UserLocationState()
}

/** 附近医院搜索状态 */
private sealed class HospitalSearchState {
    data object Idle : HospitalSearchState()
    data object Loading : HospitalSearchState()
    data class Success(val hospitals: List<NearbyHospital>) : HospitalSearchState()
    data object Empty : HospitalSearchState()
    data class Error(val message: String) : HospitalSearchState()
}

/**
 * Hospital picker dialog.
 *
 * 使用三家地图 SDK 实现地图展示和附近医院搜索：
 * - 地图视图：MapViewSwitcher（高德 → 百度 → 腾讯 自动降级）
 * - 医院搜索：PoiSearchManager（百度 → 高德 → 腾讯 自动降级）
 * - 定位：优先使用 LocationHelper，回退到 LocationFallbackManager
 *
 * @param currentSettings current app settings
 * @param locationHelper 外部传入的定位工具实例（可选，由 Activity 管理生命周期）
 * @param onSave callback with updated settings
 * @param onDismiss dismiss callback
 */
@Composable
fun HospitalPickerDialog(
    currentSettings: AppSettings,
    locationHelper: LocationHelper? = null,
    onSave: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = backgroundAwareColors()
    val context = LocalContext.current
    val locHelper = locationHelper ?: remember { LocationHelper.create(context) }
    val scope = rememberCoroutineScope()

    var customName by remember { mutableStateOf("") }
    var customAddress by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0=附近医院, 1=自定义

    var userLocationState by remember { mutableStateOf<UserLocationState>(UserLocationState.Loading) }
    var hospitalSearchState by remember { mutableStateOf<HospitalSearchState>(HospitalSearchState.Idle) }
    var selectedHospital by remember { mutableStateOf<NearbyHospital?>(null) }

    // 定位权限检查 + 发起定位 + 搜索附近医院
    LaunchedEffect(Unit) {
        if (!LocationHelper.hasAnyLocationPermission(context)) {
            userLocationState = UserLocationState.Error("定位权限未授予，无法获取附近医院")
            return@LaunchedEffect
        }

        // 先用 LocationHelper 尝试定位
        locHelper.requestSingleUpdate { result ->
            when (result) {
                is LocationHelper.Result.Success -> {
                    userLocationState = UserLocationState.Success(
                        lat = result.location.latitude,
                        lng = result.location.longitude,
                        accuracy = result.location.accuracy,
                        vendor = "系统定位"
                    )
                    searchHospitals(
                        scope, context,
                        result.location.latitude,
                        result.location.longitude
                    ) { hospitalSearchState = it }
                }
                is LocationHelper.Result.CoarseOnly -> {
                    userLocationState = UserLocationState.Success(
                        lat = result.location.latitude,
                        lng = result.location.longitude,
                        accuracy = result.location.accuracy,
                        vendor = "系统定位(粗略)"
                    )
                    searchHospitals(
                        scope, context,
                        result.location.latitude,
                        result.location.longitude
                    ) { hospitalSearchState = it }
                }
                is LocationHelper.Result.LocationDisabled -> {
                    // 系统定位失败，尝试 SDK 定位降级
                    scope.launch {
                        val sdkResult = LocationFallbackManager.getLocation(context)
                        when (sdkResult) {
                            is LocationFallbackManager.LocationResult.Success -> {
                                userLocationState = UserLocationState.Success(
                                    lat = sdkResult.lat,
                                    lng = sdkResult.lng,
                                    accuracy = sdkResult.accuracy,
                                    vendor = sdkResult.vendor
                                )
                                searchHospitals(
                                    scope, context,
                                    sdkResult.lat, sdkResult.lng
                                ) { hospitalSearchState = it }
                            }
                            else -> {
                                userLocationState = UserLocationState.Error("系统定位开关未开启，请前往系统设置打开定位")
                            }
                        }
                    }
                }
                is LocationHelper.Result.PermissionDenied ->
                    userLocationState = UserLocationState.Error("定位权限未授予，无法获取附近医院")
                is LocationHelper.Result.Timeout -> {
                    // 超时，尝试 SDK 定位降级
                    scope.launch {
                        val sdkResult = LocationFallbackManager.getLocation(context)
                        when (sdkResult) {
                            is LocationFallbackManager.LocationResult.Success -> {
                                userLocationState = UserLocationState.Success(
                                    lat = sdkResult.lat,
                                    lng = sdkResult.lng,
                                    accuracy = sdkResult.accuracy,
                                    vendor = sdkResult.vendor
                                )
                                searchHospitals(
                                    scope, context,
                                    sdkResult.lat, sdkResult.lng
                                ) { hospitalSearchState = it }
                            }
                            else -> {
                                userLocationState = UserLocationState.Error("定位超时，请检查系统定位设置后重试")
                            }
                        }
                    }
                }
                is LocationHelper.Result.NoManager ->
                    userLocationState = UserLocationState.Error("定位服务不可用")
            }
        }
    }

    // 页面销毁时取消搜索
    DisposableEffect(Unit) {
        onDispose {
            PoiSearchManager.cancel()
            LocationFallbackManager.cancel()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(LiquidGlassColors.LightBackground)
                .padding(0.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "选择透析医院",
                        style = ClearDuTypography.MedPageTitle,
                        color = LiquidGlassColors.LightForeground
                    )
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = colors.text400)
                    }
                }

                // Tab row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabChip("附近医院", selected = selectedTab == 0) { selectedTab = 0 }
                    TabChip("自定义", selected = selectedTab == 1) { selectedTab = 1 }
                }

                Spacer(Modifier.height(8.dp))

                if (selectedTab == 0) {
                    // === 附近医院标签页：地图 + 列表 ===
                    when (val locState = userLocationState) {
                        is UserLocationState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 3.dp,
                                        color = LiquidGlassColors.MedicalCyan
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text("正在获取位置...", style = ClearDuTypography.MedDetail, color = colors.text400)
                                }
                            }
                        }
                        is UserLocationState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Text(locState.message, style = ClearDuTypography.MedDetail, color = LiquidGlassColors.MedicalOrange)
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "请切换到「自定义」标签页手动输入",
                                        style = ClearDuTypography.MedCardMeta,
                                        color = colors.text400
                                    )
                                }
                            }
                        }
                        is UserLocationState.Success -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // 地图区域
                                HospitalMapView(
                                    userLat = locState.lat,
                                    userLng = locState.lng,
                                    hospitals = when (val hState = hospitalSearchState) {
                                        is HospitalSearchState.Success -> hState.hospitals
                                        else -> emptyList()
                                    },
                                    onHospitalClick = { hospital ->
                                        selectedHospital = hospital
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.5f)
                                )

                                HorizontalDivider(
                                    color = LiquidGlassColors.GlassBorder,
                                    thickness = 1.dp
                                )

                                // 医院列表区域
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "附近医院",
                                            style = ClearDuTypography.MedListTitle,
                                            color = LiquidGlassColors.LightForeground
                                        )
                                        Text(
                                            "${locState.vendor} 精度 ${"%.0f".format(locState.accuracy)}m",
                                            style = ClearDuTypography.MedCardMeta,
                                            color = colors.text400
                                        )
                                    }

                                    when (val hState = hospitalSearchState) {
                                        is HospitalSearchState.Idle,
                                        is HospitalSearchState.Loading -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(vertical = 16.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = LiquidGlassColors.MedicalCyan
                                                )
                                                Text("正在搜索附近医院...", style = ClearDuTypography.MedDetail, color = colors.text400)
                                            }
                                        }
                                        is HospitalSearchState.Success -> {
                                            if (hState.hospitals.isEmpty()) {
                                                Text(
                                                    "未找到附近医院，请切换到「自定义」标签页手动输入",
                                                    style = ClearDuTypography.MedDetail,
                                                    color = colors.text400,
                                                    modifier = Modifier.padding(vertical = 16.dp)
                                                )
                                            } else {
                                                hState.hospitals.forEach { hospital ->
                                                    val isSelected = selectedHospital == hospital
                                                    HospitalListItem(
                                                        hospital = hospital,
                                                        isSelected = isSelected,
                                                        onClick = {
                                                            selectedHospital = hospital
                                                            onSave(
                                                                currentSettings.copy(
                                                                    hospitalName = hospital.name,
                                                                    hospitalAddress = hospital.address,
                                                                    hospitalIsCustom = false
                                                                )
                                                            )
                                                            onDismiss()
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        is HospitalSearchState.Empty -> {
                                            Text(
                                                "在您周围 20km 范围内未找到医院。请切换到「自定义」标签页手动输入。",
                                                style = ClearDuTypography.MedDetail,
                                                color = colors.text400,
                                                modifier = Modifier.padding(vertical = 16.dp)
                                            )
                                        }
                                        is HospitalSearchState.Error -> {
                                            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                                Text(hState.message, style = ClearDuTypography.MedDetail, color = LiquidGlassColors.MedicalOrange)
                                                Spacer(Modifier.height(6.dp))
                                                Text("请切换到「自定义」标签页手动输入。", style = ClearDuTypography.MedDetail, color = colors.text400)
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                } else {
                    // === 自定义标签页 ===
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            specularTop = LiquidGlassColors.GlassSpecularTop
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("医院名称", style = ClearDuTypography.MedListTitle, color = colors.text400)
                                Spacer(Modifier.height(8.dp))
                                BasicTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = ClearDuTypography.MedSearchPlaceholder.copy(
                                        color = LiquidGlassColors.LightForeground
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                                    decorationBox = { inner ->
                                        if (customName.isEmpty()) {
                                            Text("输入医院名称", style = ClearDuTypography.MedSearchPlaceholder, color = LiquidGlassColors.PlaceholderInput)
                                        }
                                        inner()
                                    }
                                )
                            }
                        }

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            specularTop = LiquidGlassColors.GlassSpecularTop
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("医院地址（可选）", style = ClearDuTypography.MedListTitle, color = colors.text400)
                                Spacer(Modifier.height(8.dp))
                                BasicTextField(
                                    value = customAddress,
                                    onValueChange = { customAddress = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = ClearDuTypography.MedSearchPlaceholder.copy(
                                        color = LiquidGlassColors.LightForeground
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                                    decorationBox = { inner ->
                                        if (customAddress.isEmpty()) {
                                            Text("输入医院地址", style = ClearDuTypography.MedSearchPlaceholder, color = LiquidGlassColors.PlaceholderInput)
                                        }
                                        inner()
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Save custom button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (customName.isNotBlank()) LiquidGlassColors.MedicalCyan
                                    else LiquidGlassColors.Text400.copy(alpha = 0.3f)
                                )
                                .clickable(enabled = customName.isNotBlank()) {
                                    onSave(
                                        currentSettings.copy(
                                            hospitalName = customName.trim(),
                                            hospitalAddress = customAddress.trim(),
                                            hospitalIsCustom = true
                                        )
                                    )
                                    onDismiss()
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "保存自定义医院",
                                style = ClearDuTypography.MedTakeBtn,
                                color = LiquidGlassColors.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 搜索附近医院，优先使用 PoiSearchManager，回退到 NearbySearchService。
 */
private fun searchHospitals(
    scope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
    lat: Double,
    lng: Double,
    onState: (HospitalSearchState) -> Unit
) {
    onState(HospitalSearchState.Loading)
    scope.launch {
        try {
            // 优先使用三家 SDK POI 搜索
            val sdkResults = PoiSearchManager.searchNearbyHospitals(context, lat, lng)
            if (sdkResults.isNotEmpty()) {
                onState(HospitalSearchState.Success(sdkResults))
                return@launch
            }

            // 回退到 Geocoder + Overpass API
            val fallbackResults = com.cleardu.app.util.NearbySearchService.searchNearbyHospitals(
                context, lat, lng
            )
            onState(
                if (fallbackResults.isEmpty()) HospitalSearchState.Empty
                else HospitalSearchState.Success(fallbackResults)
            )
        } catch (e: Exception) {
            onState(
                HospitalSearchState.Error(
                    "搜索附近医院失败：${e.message ?: "网络异常"}"
                )
            )
        }
    }
}

/**
 * 医院列表项组件
 */
@Composable
private fun HospitalListItem(
    hospital: NearbyHospital,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = backgroundAwareColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) LiquidGlassColors.MedicalCyan.copy(alpha = 0.12f)
                else LiquidGlassColors.GlassBg
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    hospital.name,
                    style = ClearDuTypography.MedItemName,
                    color = if (isSelected) LiquidGlassColors.MedicalCyan else LiquidGlassColors.LightForeground
                )
                if (hospital.address.isNotBlank()) {
                    Text(
                        hospital.address,
                        style = ClearDuTypography.MedDetail,
                        color = colors.text400,
                        maxLines = 1
                    )
                }
            }
            if (hospital.distance.isNotBlank()) {
                Text(
                    hospital.distance,
                    style = ClearDuTypography.MedCardMeta,
                    color = if (isSelected) LiquidGlassColors.MedicalCyan else colors.text400,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
internal fun TabChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = backgroundAwareColors()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) LiquidGlassColors.MedicalCyan.copy(alpha = 0.15f) else LiquidGlassColors.GlassBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = ClearDuTypography.MedCardMeta,
            color = if (selected) LiquidGlassColors.MedicalCyan else colors.text400
        )
    }
}