package com.cleardu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cleardu.app.data.AppSettings
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Medication reminder settings dialog.
 *
 * Allows configuring reminder toggle, advance time, and viewing
 * the list of custom medications with their schedules.
 *
 * @param currentSettings current app settings
 * @param onSave callback with updated settings
 * @param onDismiss dismiss callback
 */
@Composable
fun MedicationSettingsDialog(
    currentSettings: AppSettings,
    onSave: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var reminderEnabled by remember { mutableStateOf(currentSettings.medicationReminderEnabled) }
    var advanceMinutes by remember { mutableIntStateOf(currentSettings.medicationReminderAdvanceMinutes) }

    val advanceOptions = listOf(5, 10, 15, 30, 60)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.7f)
                .clip(RoundedCornerShape(24.dp))
                .background(LiquidGlassColors.LightBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "用药提醒设置",
                        style = ClearDuTypography.MedPageTitle,
                        color = LiquidGlassColors.LightForeground
                    )
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = LiquidGlassColors.Text400)
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Toggle
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        background = LiquidGlassColors.GlassBg,
                        border = LiquidGlassColors.GlassBorder,
                        specularTop = LiquidGlassColors.GlassSpecularTop
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "启用用药提醒",
                                    style = ClearDuTypography.MedItemName,
                                    color = LiquidGlassColors.LightForeground
                                )
                                Text(
                                    text = "在设定的时间提醒您服药",
                                    style = ClearDuTypography.MedDetail,
                                    color = LiquidGlassColors.Text400,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            ReminderToggle(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it }
                            )
                        }
                    }

                    if (reminderEnabled) {
                        // Advance time
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            background = LiquidGlassColors.GlassBg,
                            border = LiquidGlassColors.GlassBorder,
                            specularTop = LiquidGlassColors.GlassSpecularTop
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "提前提醒时间",
                                    style = ClearDuTypography.MedItemName,
                                    color = LiquidGlassColors.LightForeground
                                )
                                Text(
                                    text = "在服药时间前${advanceMinutes}分钟发送提醒",
                                    style = ClearDuTypography.MedDetail,
                                    color = LiquidGlassColors.Text400,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    advanceOptions.forEach { mins ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    if (advanceMinutes == mins) LiquidGlassColors.MedicalCyan.copy(alpha = 0.15f)
                                                    else LiquidGlassColors.GlassBg
                                                )
                                                .drawBehind {
                                                    if (advanceMinutes == mins) {
                                                        drawRect(LiquidGlassColors.MedicalCyan.copy(alpha = 0.15f))
                                                    }
                                                }
                                                .clickable { advanceMinutes = mins }
                                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = "${mins}分钟",
                                                style = ClearDuTypography.MedDoseBtn,
                                                color = if (advanceMinutes == mins) LiquidGlassColors.MedicalCyan else LiquidGlassColors.Text400
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Custom medications list
                    if (currentSettings.customMedications.isNotEmpty()) {
                        Text(
                            text = "已添加的自定义用药",
                            style = ClearDuTypography.MedSectionLabel,
                            color = LiquidGlassColors.Text400,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )

                        currentSettings.customMedications.filter { it.isActive }.forEach { med ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
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
                                            text = med.name,
                                            style = ClearDuTypography.MedItemName,
                                            color = LiquidGlassColors.LightForeground
                                        )
                                        Text(
                                            text = "${med.detail} · ${med.frequency} ${med.times.joinToString(", ")}",
                                            style = ClearDuTypography.MedDetail,
                                            color = LiquidGlassColors.Text400
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Save button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(LiquidGlassColors.MedicalCyan)
                            .clickable {
                                onSave(
                                    currentSettings.copy(
                                        medicationReminderEnabled = reminderEnabled,
                                        medicationReminderAdvanceMinutes = advanceMinutes
                                    )
                                )
                                onDismiss()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "保存设置",
                            style = ClearDuTypography.MedTakeBtn,
                            color = LiquidGlassColors.White
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}