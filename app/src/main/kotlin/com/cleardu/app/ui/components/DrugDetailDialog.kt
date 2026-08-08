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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cleardu.app.data.DrugInfo
import com.cleardu.app.data.MedicationDose
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors

/**
 * Drug detail dialog showing comprehensive drug information
 * similar to a package insert (说明书).
 *
 * @param drug the drug information to display
 * @param onAdd callback to add this drug to the medication list
 * @param onDismiss dismiss callback
 */
@Composable
fun DrugDetailDialog(
    drug: DrugInfo,
    onAdd: (MedicationDose) -> Unit = {},
    onDismiss: () -> Unit
) {
    val colors = backgroundAwareColors()
    var selectedDose by remember { mutableIntStateOf(1) } // 0=0.5, 1=1, 2=2
    var recordTime by remember { mutableStateOf("08:00") }

    val times = listOf("08:00", "12:00", "18:00", "21:00")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(LiquidGlassColors.LightBackground)
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
                    Column {
                        Text(
                            text = drug.name,
                            style = ClearDuTypography.MedPageTitle,
                            color = LiquidGlassColors.LightForeground
                        )
                        if (drug.genericName.isNotEmpty()) {
                            Text(
                                text = drug.genericName,
                                style = ClearDuTypography.MedDetail,
                                color = colors.text400
                            )
                        }
                        if (drug.isFromNetwork) {
                            Text(
                                text = "来自网络 · 仅供参考",
                                style = ClearDuTypography.MedCardMeta,
                                color = LiquidGlassColors.MedicalOrange,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("关闭", color = colors.text400)
                    }
                }

                // Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category
                    if (drug.category.isNotEmpty()) {
                        DrugSection("药品分类", drug.category)
                    }

                    // Manufacturer
                    if (drug.manufacturer.isNotEmpty()) {
                        DrugSection("生产厂家", drug.manufacturer)
                    }

                    // Description
                    if (drug.description.isNotEmpty()) {
                        DrugSection("药品描述", drug.description)
                    }

                    // Indications
                    if (drug.indications.isNotEmpty()) {
                        DrugSection("适应症", drug.indications)
                    }

                    // Dosage
                    if (drug.dosage.isNotEmpty()) {
                        DrugSection("用法用量", drug.dosage)
                    }

                    // Administration
                    if (drug.administration.isNotEmpty()) {
                        DrugSection("给药说明", drug.administration)
                    }

                    // Side effects
                    if (drug.sideEffects.isNotEmpty()) {
                        DrugSection("不良反应", drug.sideEffects)
                    }

                    // Warnings
                    if (drug.warnings.isNotEmpty()) {
                        DrugSection("注意事项", drug.warnings)
                    }

                    // Contraindications
                    if (drug.contraindications.isNotEmpty()) {
                        DrugSection("禁忌", drug.contraindications)
                    }

                    // Drug interactions
                    if (drug.drugInteractions.isNotEmpty()) {
                        DrugSection("药物相互作用", drug.drugInteractions)
                    }

                    // Storage
                    if (drug.storageInfo.isNotEmpty()) {
                        DrugSection("贮藏", drug.storageInfo)
                    }

                    // Package
                    if (drug.packageInfo.isNotEmpty()) {
                        DrugSection("包装规格", drug.packageInfo)
                    }

                    Spacer(Modifier.height(8.dp))
                }

                // Bottom: record controls
                Divider(modifier = Modifier.padding(horizontal = 20.dp))

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dose selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("记录用量", style = ClearDuTypography.MedItemName, color = LiquidGlassColors.LightForeground)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("0.5", "1", "2").forEachIndexed { index, label ->
                                DoseChip(
                                    label = label,
                                    selected = selectedDose == index,
                                    onClick = { selectedDose = index }
                                )
                            }
                        }
                    }

                    // Time selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("记录时间", style = ClearDuTypography.MedItemName, color = LiquidGlassColors.LightForeground)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            times.forEach { time ->
                                TimeChip(
                                    time = time,
                                    selected = recordTime == time,
                                    onClick = { recordTime = time }
                                )
                            }
                        }
                    }

                    // Add to record button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(LiquidGlassColors.MedicalCyan)
                            .clickable {
                                val multiplier = when (selectedDose) {
                                    0 -> 0.5
                                    2 -> 2.0
                                    else -> 1.0
                                }
                                val detail = if (drug.dosage.isNotEmpty()) drug.dosage.take(50) else drug.genericName
                                onAdd(MedicationDose(name = drug.name, detail = detail, doseMultiplier = multiplier))
                                onDismiss()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "记录用药",
                            style = ClearDuTypography.MedTakeBtn,
                            color = LiquidGlassColors.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrugSection(title: String, content: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = ClearDuTypography.MedItemName,
                color = LiquidGlassColors.MedicalCyan,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = content,
                style = ClearDuTypography.MedDetail.copy(fontSize = 13.sp, lineHeight = 20.sp),
                color = LiquidGlassColors.LightForeground
            )
        }
    }
}

@Composable
private fun Divider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LiquidGlassColors.LightDividerSubtle)
    )
}

@Composable
private fun DoseChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = backgroundAwareColors()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) LiquidGlassColors.MedicalCyan.copy(alpha = 0.15f) else LiquidGlassColors.GlassBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = ClearDuTypography.MedDoseBtn,
            color = if (selected) LiquidGlassColors.MedicalCyan else colors.text400
        )
    }
}

@Composable
private fun TimeChip(time: String, selected: Boolean, onClick: () -> Unit) {
    val colors = backgroundAwareColors()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) LiquidGlassColors.MedicalCyan.copy(alpha = 0.15f) else LiquidGlassColors.GlassBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = time,
            style = ClearDuTypography.MedDoseBtn,
            color = if (selected) LiquidGlassColors.MedicalCyan else colors.text400
        )
    }
}