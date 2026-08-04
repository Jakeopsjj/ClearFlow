package com.cleardu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cleardu.app.data.CustomMedication
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Dialog for adding a custom medication.
 *
 * Fields: name, detail (dosage info), frequency, times, notes.
 *
 * @param onSave callback with the new medication
 * @param onDismiss dismiss callback
 */
@Composable
fun AddMedicationDialog(
    onSave: (CustomMedication) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableIntStateOf(0) } // 0=每日, 1=隔日, 2=每周
    var selectedTime by remember { mutableIntStateOf(0) } // 0=08:00, 1=12:00, 2=18:00, 3=21:00
    var notes by remember { mutableStateOf("") }

    val frequencies = listOf("每日", "隔日", "每周")
    val times = listOf("08:00", "12:00", "18:00", "21:00")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(LiquidGlassColors.LightBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        text = "添加自定义用药",
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
                    // Drug name
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        background = LiquidGlassColors.GlassBg,
                        border = LiquidGlassColors.GlassBorder,
                        specularTop = LiquidGlassColors.GlassSpecularTop
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("药品名称 *", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.Text400)
                            Spacer(Modifier.height(8.dp))
                            BasicTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = ClearDuTypography.MedSearchPlaceholder.copy(color = LiquidGlassColors.Foreground),
                                singleLine = true,
                                cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                                decorationBox = { inner ->
                                    if (name.isEmpty()) {
                                        Text("例如：降压药", style = ClearDuTypography.MedSearchPlaceholder, color = LiquidGlassColors.PlaceholderInput)
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    // Dosage detail
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        background = LiquidGlassColors.GlassBg,
                        border = LiquidGlassColors.GlassBorder,
                        specularTop = LiquidGlassColors.GlassSpecularTop
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("剂量详情 *", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.Text400)
                            Spacer(Modifier.height(8.dp))
                            BasicTextField(
                                value = detail,
                                onValueChange = { detail = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = ClearDuTypography.MedSearchPlaceholder.copy(color = LiquidGlassColors.Foreground),
                                singleLine = true,
                                cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                                decorationBox = { inner ->
                                    if (detail.isEmpty()) {
                                        Text("例如：缬沙坦 80mg", style = ClearDuTypography.MedSearchPlaceholder, color = LiquidGlassColors.PlaceholderInput)
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    // Frequency
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        background = LiquidGlassColors.GlassBg,
                        border = LiquidGlassColors.GlassBorder,
                        specularTop = LiquidGlassColors.GlassSpecularTop
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("服药频率", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.Text400)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                frequencies.forEachIndexed { index, freq ->
                                    FrequencyChip(
                                        text = freq,
                                        selected = selectedFrequency == index,
                                        onClick = { selectedFrequency = index }
                                    )
                                }
                            }
                        }
                    }

                    // Time
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        background = LiquidGlassColors.GlassBg,
                        border = LiquidGlassColors.GlassBorder,
                        specularTop = LiquidGlassColors.GlassSpecularTop
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("服药时间", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.Text400)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                times.forEachIndexed { index, time ->
                                    FrequencyChip(
                                        text = time,
                                        selected = selectedTime == index,
                                        onClick = { selectedTime = index }
                                    )
                                }
                            }
                        }
                    }

                    // Notes
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        background = LiquidGlassColors.GlassBg,
                        border = LiquidGlassColors.GlassBorder,
                        specularTop = LiquidGlassColors.GlassSpecularTop
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("备注（可选）", style = ClearDuTypography.MedListTitle, color = LiquidGlassColors.Text400)
                            Spacer(Modifier.height(8.dp))
                            BasicTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                                textStyle = ClearDuTypography.MedSearchPlaceholder.copy(color = LiquidGlassColors.Foreground),
                                cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                                decorationBox = { inner ->
                                    if (notes.isEmpty()) {
                                        Text("用药注意事项...", style = ClearDuTypography.MedSearchPlaceholder, color = LiquidGlassColors.PlaceholderInput)
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    // Save button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (name.isNotBlank() && detail.isNotBlank()) LiquidGlassColors.MedicalCyan
                                else LiquidGlassColors.Text400.copy(alpha = 0.3f)
                            )
                            .clickable(enabled = name.isNotBlank() && detail.isNotBlank()) {
                                onSave(
                                    CustomMedication(
                                        name = name.trim(),
                                        detail = detail.trim(),
                                        frequency = frequencies[selectedFrequency],
                                        times = listOf(times[selectedTime]),
                                        notes = notes.trim()
                                    )
                                )
                                onDismiss()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "添加用药",
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

@Composable
private fun FrequencyChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) LiquidGlassColors.MedicalCyan.copy(alpha = 0.15f) else LiquidGlassColors.GlassBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = ClearDuTypography.MedDoseBtn,
            color = if (selected) LiquidGlassColors.MedicalCyan else LiquidGlassColors.Text400
        )
    }
}