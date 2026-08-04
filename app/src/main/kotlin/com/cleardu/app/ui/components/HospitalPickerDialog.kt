package com.cleardu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cleardu.app.data.AppSettings
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Nearby hospital data for selection.
 * In a production app this would come from a location API (e.g. Amap/Google Maps).
 */
data class NearbyHospital(
    val name: String,
    val address: String,
    val distance: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

/** Default nearby hospitals list (mock data for demo). */
private val nearbyHospitals = listOf(
    NearbyHospital("北京大学第一医院 血液净化中心", "西城区西什库大街8号", "约2.3km"),
    NearbyHospital("北京协和医院 肾内科血透室", "东城区东单北大街53号", "约3.1km"),
    NearbyHospital("中日友好医院 血液净化中心", "朝阳区樱花园东街2号", "约4.5km"),
    NearbyHospital("北京友谊医院 透析中心", "西城区永安路95号", "约5.0km"),
    NearbyHospital("解放军总医院 肾内科透析室", "海淀区复兴路28号", "约6.2km"),
    NearbyHospital("北京安贞医院 血液净化科", "朝阳区安贞路2号", "约5.8km"),
    NearbyHospital("北京朝阳医院 透析中心", "朝阳区工体南路8号", "约3.7km"),
    NearbyHospital("北京天坛医院 肾内科", "丰台区南四环西路119号", "约8.3km")
)

/**
 * Hospital picker dialog.
 *
 * Shows nearby hospitals for selection, plus a custom input option.
 * User can search nearby hospitals or enter a custom hospital name.
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
    var searchQuery by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customAddress by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0=附近医院, 1=自定义

    val filteredHospitals = remember(searchQuery, selectedTab) {
        if (selectedTab == 0 && searchQuery.isNotBlank()) {
            nearbyHospitals.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
            }
        } else {
            nearbyHospitals
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
                    // Search bar
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(14.dp),
                        background = LiquidGlassColors.GlassBg,
                        border = LiquidGlassColors.GlassBorder,
                        specularTop = LiquidGlassColors.GlassSpecularTop
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            textStyle = ClearDuTypography.MedSearchPlaceholder.copy(
                                color = LiquidGlassColors.Foreground
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                            decorationBox = { inner ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "搜索附近医院...",
                                            style = ClearDuTypography.MedSearchPlaceholder,
                                            color = LiquidGlassColors.PlaceholderInput
                                        )
                                    }
                                    inner()
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Hospital list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredHospitals) { hospital ->
                            HospitalItem(
                                hospital = hospital,
                                isSelected = currentSettings.hospitalName == hospital.name,
                                onClick = {
                                    onSave(
                                        currentSettings.copy(
                                            hospitalName = hospital.name,
                                            hospitalAddress = hospital.address,
                                            hospitalLat = hospital.lat,
                                            hospitalLng = hospital.lng,
                                            hospitalIsCustom = false
                                        )
                                    )
                                    onDismiss()
                                }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                } else {
                    // Custom input
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                        color = LiquidGlassColors.Foreground
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
                                        color = LiquidGlassColors.Foreground
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
private fun HospitalItem(
    hospital: NearbyHospital,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        background = if (isSelected) LiquidGlassColors.TintCyanMd else LiquidGlassColors.GlassBg,
        border = if (isSelected) LiquidGlassColors.MedicalCyan.copy(alpha = 0.3f) else LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hospital.name,
                    style = ClearDuTypography.MedItemName,
                    color = if (isSelected) LiquidGlassColors.MedicalCyan else LiquidGlassColors.Foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = hospital.address,
                    style = ClearDuTypography.MedDetail,
                    color = LiquidGlassColors.Text400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (hospital.distance.isNotEmpty()) {
                Text(
                    text = hospital.distance,
                    style = ClearDuTypography.MedCardMeta,
                    color = LiquidGlassColors.Text400
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