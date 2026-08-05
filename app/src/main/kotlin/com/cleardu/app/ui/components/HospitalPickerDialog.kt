package com.cleardu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
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
import com.cleardu.app.util.LocationHelper
import com.cleardu.app.util.NearbySearchService
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

/**
 * 用户当前位置状态，用于 HospitalPickerDialog 内部定位。
 */
private sealed class UserLocationState {
    data object Loading : UserLocationState()
    data class Success(val lat: Double, val lng: Double, val accuracy: Float) : UserLocationState()
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
 * 通过 OpenStreetMap Overpass API 自动搜索用户当前位置附近的医院。
 * 无需第三方 API Key，免费使用。
 *
 * @param currentSettings current app settings
 * @param onSave callback with updated settings
 * @param onDismiss dismiss callback
 */
@Composable
fun HospitalPickerDialog(
    currentSettings: AppSettings,
    onSave: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper.create(context) }
    val scope = rememberCoroutineScope()

    var customName by remember { mutableStateOf("") }
    var customAddress by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0=附近医院, 1=自定义

    var userLocationState by remember { mutableStateOf<UserLocationState>(UserLocationState.Loading) }
    var hospitalSearchState by remember { mutableStateOf<HospitalSearchState>(HospitalSearchState.Idle) }

    // 定位权限检查 + 发起定位
    LaunchedEffect(Unit) {
        if (!LocationHelper.hasAnyLocationPermission(context)) {
            userLocationState = UserLocationState.Error("定位权限未授予，无法获取附近医院")
            return@LaunchedEffect
        }
        locationHelper.requestSingleUpdate { result ->
            userLocationState = when (result) {
                is LocationHelper.Result.Success -> {
                    val state = UserLocationState.Success(result.location.latitude, result.location.longitude, result.location.accuracy)
                    // 定位成功后自动搜索附近医院
                    if (hospitalSearchState is HospitalSearchState.Idle) {
                        hospitalSearchState = HospitalSearchState.Loading
                        scope.launch {
                            try {
                                val hospitals = NearbySearchService.searchNearbyHospitals(
                                    result.location.latitude, result.location.longitude
                                )
                                hospitalSearchState = if (hospitals.isEmpty()) {
                                    HospitalSearchState.Empty
                                } else {
                                    HospitalSearchState.Success(hospitals)
                                }
                            } catch (e: Exception) {
                                hospitalSearchState = HospitalSearchState.Error(
                                    "搜索附近医院失败：${e.message ?: "网络异常"}"
                                )
                            }
                        }
                    }
                    state
                }
                is LocationHelper.Result.CoarseOnly -> {
                    val state = UserLocationState.Success(result.location.latitude, result.location.longitude, result.location.accuracy)
                    if (hospitalSearchState is HospitalSearchState.Idle) {
                        hospitalSearchState = HospitalSearchState.Loading
                        scope.launch {
                            try {
                                val hospitals = NearbySearchService.searchNearbyHospitals(
                                    result.location.latitude, result.location.longitude
                                )
                                hospitalSearchState = if (hospitals.isEmpty()) {
                                    HospitalSearchState.Empty
                                } else {
                                    HospitalSearchState.Success(hospitals)
                                }
                            } catch (e: Exception) {
                                hospitalSearchState = HospitalSearchState.Error(
                                    "搜索附近医院失败：${e.message ?: "网络异常"}"
                                )
                            }
                        }
                    }
                    state
                }
                is LocationHelper.Result.LocationDisabled -> {
                    UserLocationState.Error("系统定位开关未开启，请前往系统设置打开定位")
                }
                is LocationHelper.Result.PermissionDenied -> {
                    UserLocationState.Error("定位权限未授予，无法获取附近医院")
                }
                is LocationHelper.Result.Timeout -> {
                    UserLocationState.Error("定位超时，请检查系统定位设置后重试")
                }
                is LocationHelper.Result.NoManager -> {
                    UserLocationState.Error("定位服务不可用")
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(24.dp))
                .background(LiquidGlassColors.LightBackground)
                .padding(0.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "选择透析医院",
                        style = ClearDuTypography.MedPageTitle,
                        color = LiquidGlassColors.LightForeground
                    )
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = LiquidGlassColors.Text400)
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

                Spacer(Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // 附近医院标签页：展示用户位置 + 搜索到的附近医院列表
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 用户位置卡片
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            background = LiquidGlassColors.GlassBg,
                            border = LiquidGlassColors.GlassBorder,
                            specularTop = LiquidGlassColors.GlassSpecularTop
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "我的位置",
                                    style = ClearDuTypography.MedListTitle,
                                    color = LiquidGlassColors.Text400
                                )
                                Spacer(Modifier.height(8.dp))
                                when (val state = userLocationState) {
                                    is UserLocationState.Loading -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = LiquidGlassColors.MedicalCyan
                                            )
                                            Text("正在获取您的位置...", style = ClearDuTypography.MedDetail, color = LiquidGlassColors.Text400)
                                        }
                                    }
                                    is UserLocationState.Success -> {
                                        Text("纬度: ${"%.6f".format(state.lat)}", style = ClearDuTypography.MedDetail, color = LiquidGlassColors.LightForeground)
                                        Text("经度: ${"%.6f".format(state.lng)}", style = ClearDuTypography.MedDetail, color = LiquidGlassColors.LightForeground)
                                        Text("精度: ${"%.0f".format(state.accuracy)}m", style = ClearDuTypography.MedCardMeta, color = LiquidGlassColors.Text400)
                                    }
                                    is UserLocationState.Error -> {
                                        Text(state.message, style = ClearDuTypography.MedDetail, color = LiquidGlassColors.MedicalOrange)
                                    }
                                }
                            }
                        }

                        // 附近医院搜索结果
                        Text(
                            "附近医院",
                            style = ClearDuTypography.MedListTitle,
                            color = LiquidGlassColors.LightForeground,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        when (val hState = hospitalSearchState) {
                            is HospitalSearchState.Idle -> {
                                // 等待定位完成
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = LiquidGlassColors.MedicalCyan
                                    )
                                    Text("等待定位...", style = ClearDuTypography.MedDetail, color = LiquidGlassColors.Text400)
                                }
                            }
                            is HospitalSearchState.Loading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = LiquidGlassColors.MedicalCyan
                                    )
                                    Text("正在搜索附近医院...", style = ClearDuTypography.MedDetail, color = LiquidGlassColors.Text400)
                                }
                            }
                            is HospitalSearchState.Success -> {
                                hState.hospitals.forEach { hospital ->
                                    NearbyHospitalCard(
                                        hospital = hospital,
                                        onClick = {
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
                            is HospitalSearchState.Empty -> {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    background = LiquidGlassColors.TintCyanBg,
                                    border = LiquidGlassColors.MedicalCyan.copy(alpha = 0.15f),
                                    specularTop = LiquidGlassColors.GlassSpecularTop
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("未找到附近医院", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.MedicalCyan)
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "在您周围 20km 范围内未找到标注为医院的设施。请切换到「自定义」标签页，手动输入您常去的透析医院名称和地址。",
                                            style = ClearDuTypography.MedDetail,
                                            color = LiquidGlassColors.Text400
                                        )
                                    }
                                }
                            }
                            is HospitalSearchState.Error -> {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    background = LiquidGlassColors.TintCyanBg,
                                    border = LiquidGlassColors.MedicalOrange.copy(alpha = 0.15f),
                                    specularTop = LiquidGlassColors.GlassSpecularTop
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("搜索失败", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.MedicalOrange)
                                        Spacer(Modifier.height(6.dp))
                                        Text(hState.message, style = ClearDuTypography.MedDetail, color = LiquidGlassColors.Text400)
                                        Spacer(Modifier.height(8.dp))
                                        Text("请切换到「自定义」标签页手动输入医院信息。", style = ClearDuTypography.MedDetail, color = LiquidGlassColors.Text400)
                                    }
                                }
                            }
                        }

                        // 已有医院信息（如之前选过）
                        if (currentSettings.hospitalName.isNotBlank()) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                background = LiquidGlassColors.GlassBg,
                                border = LiquidGlassColors.GlassBorder,
                                specularTop = LiquidGlassColors.GlassSpecularTop
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("当前选择的医院", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.Text400)
                                    Spacer(Modifier.height(6.dp))
                                    Text(currentSettings.hospitalName, style = ClearDuTypography.MedItemName, color = LiquidGlassColors.LightForeground)
                                    if (currentSettings.hospitalAddress.isNotBlank()) {
                                        Text(currentSettings.hospitalAddress, style = ClearDuTypography.MedDetail, color = LiquidGlassColors.Text400)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                } else {
                    // Custom input
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
                            background = LiquidGlassColors.GlassBg,
                            border = LiquidGlassColors.GlassBorder,
                            specularTop = LiquidGlassColors.GlassSpecularTop
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("医院名称", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.Text400)
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
                            background = LiquidGlassColors.GlassBg,
                            border = LiquidGlassColors.GlassBorder,
                            specularTop = LiquidGlassColors.GlassSpecularTop
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("医院地址（可选）", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.Text400)
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

@Composable
private fun NearbyHospitalCard(hospital: NearbyHospital, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    hospital.name,
                    style = ClearDuTypography.MedItemName,
                    color = LiquidGlassColors.LightForeground
                )
                if (hospital.address.isNotBlank()) {
                    Text(
                        hospital.address,
                        style = ClearDuTypography.MedDetail,
                        color = LiquidGlassColors.Text400,
                        maxLines = 1
                    )
                }
            }
            if (hospital.distance.isNotBlank()) {
                Text(
                    hospital.distance,
                    style = ClearDuTypography.MedCardMeta,
                    color = LiquidGlassColors.MedicalCyan,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
internal fun TabChip(text: String, selected: Boolean, onClick: () -> Unit) {
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
            color = if (selected) LiquidGlassColors.MedicalCyan else LiquidGlassColors.Text400
        )
    }
}