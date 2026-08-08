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
import com.cleardu.app.data.AppSettings
import com.cleardu.app.data.RefillRequest
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors

/**
 * Refill request dialog.
 *
 * Shows existing refill requests and allows creating new ones.
 * In production, this would connect to a pharmacy API or hospital system.
 *
 * @param currentSettings current app settings
 * @param onSave callback with updated settings
 * @param onDismiss dismiss callback
 */
@Composable
fun RefillRequestDialog(
    currentSettings: AppSettings,
    onSave: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = backgroundAwareColors()
    var showNewRequest by remember { mutableStateOf(false) }
    var medName by remember { mutableStateOf("") }
    var medDetail by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

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
                        text = "申请续药",
                        style = ClearDuTypography.MedPageTitle,
                        color = LiquidGlassColors.LightForeground
                    )
                    TextButton(onClick = onDismiss) {
                        Text("关闭", color = colors.text400)
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Existing refill requests
                    if (currentSettings.refillRequests.isNotEmpty()) {
                        Text(
                            text = "续药申请记录",
                            style = ClearDuTypography.MedSectionLabel,
                            color = colors.text400,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        currentSettings.refillRequests.forEach { request ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
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
                                            text = request.medicationName,
                                            style = ClearDuTypography.MedItemName,
                                            color = LiquidGlassColors.LightForeground
                                        )
                                        if (request.detail.isNotEmpty()) {
                                            Text(
                                                text = request.detail,
                                                style = ClearDuTypography.MedDetail,
                                                color = colors.text400
                                            )
                                        }
                                        Text(
                                            text = when (request.status) {
                                                "pending" -> "待处理"
                                                "submitted" -> "已提交"
                                                "fulfilled" -> "已完成"
                                                else -> request.status
                                            },
                                            style = ClearDuTypography.MedCardMeta,
                                            color = when (request.status) {
                                                "pending" -> LiquidGlassColors.MedicalOrange
                                                "submitted" -> LiquidGlassColors.MedicalCyan
                                                "fulfilled" -> LiquidGlassColors.MedicalGreen
                                                else -> colors.text400
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // New request form
                    if (showNewRequest) {
                        Text(
                            text = "新建续药申请",
                            style = ClearDuTypography.MedSectionLabel,
                            color = colors.text400,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            specularTop = LiquidGlassColors.GlassSpecularTop
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("药品名称 *", style = ClearDuTypography.MedListTitle, color = colors.text400)
                                Spacer(Modifier.height(8.dp))
                                BasicTextField(
                                    value = medName,
                                    onValueChange = { medName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = ClearDuTypography.MedSearchPlaceholder.copy(color = LiquidGlassColors.LightForeground),
                                    singleLine = true,
                                    cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                                    decorationBox = { inner ->
                                        if (medName.isEmpty()) {
                                            Text("药品名称", style = ClearDuTypography.MedSearchPlaceholder, color = LiquidGlassColors.PlaceholderInput)
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
                                Text("规格剂量", style = ClearDuTypography.MedListTitle, color = colors.text400)
                                Spacer(Modifier.height(8.dp))
                                BasicTextField(
                                    value = medDetail,
                                    onValueChange = { medDetail = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = ClearDuTypography.MedSearchPlaceholder.copy(color = LiquidGlassColors.LightForeground),
                                    singleLine = true,
                                    cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                                    decorationBox = { inner ->
                                        if (medDetail.isEmpty()) {
                                            Text("如：80mg×28片", style = ClearDuTypography.MedSearchPlaceholder, color = LiquidGlassColors.PlaceholderInput)
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
                                Text("备注（可选）", style = ClearDuTypography.MedListTitle, color = colors.text400)
                                Spacer(Modifier.height(8.dp))
                                BasicTextField(
                                    value = notes,
                                    onValueChange = { notes = it },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                                    textStyle = ClearDuTypography.MedSearchPlaceholder.copy(color = LiquidGlassColors.LightForeground),
                                    cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                                    decorationBox = { inner ->
                                        if (notes.isEmpty()) {
                                            Text("续药原因或备注...", style = ClearDuTypography.MedSearchPlaceholder, color = LiquidGlassColors.PlaceholderInput)
                                        }
                                        inner()
                                    }
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (medName.isNotBlank()) LiquidGlassColors.MedicalCyan
                                    else LiquidGlassColors.Text400.copy(alpha = 0.3f)
                                )
                                .clickable(enabled = medName.isNotBlank()) {
                                    val newRequest = RefillRequest(
                                        medicationName = medName.trim(),
                                        detail = medDetail.trim(),
                                        notes = notes.trim()
                                    )
                                    onSave(
                                        currentSettings.copy(
                                            refillRequests = currentSettings.refillRequests + newRequest
                                        )
                                    )
                                    showNewRequest = false
                                    medName = ""
                                    medDetail = ""
                                    notes = ""
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "提交续药申请",
                                style = ClearDuTypography.MedTakeBtn,
                                color = LiquidGlassColors.White
                            )
                        }
                    } else {
                        // New request button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(LiquidGlassColors.MedicalCyan.copy(alpha = 0.1f))
                                .clickable { showNewRequest = true }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "+ 新建续药申请",
                                style = ClearDuTypography.MedTakeBtn,
                                color = LiquidGlassColors.MedicalCyan
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun RefillStatusBadge(status: String, modifier: Modifier = Modifier) {
    val colors = backgroundAwareColors()
    val (bg, text, label) = when (status) {
        "pending" -> Triple(LiquidGlassColors.LightTintOrangeBg, LiquidGlassColors.MedicalOrange, "待处理")
        "submitted" -> Triple(LiquidGlassColors.LightTintCyanBg, LiquidGlassColors.MedicalCyan, "已提交")
        "fulfilled" -> Triple(LiquidGlassColors.LightTintGreenStrong, LiquidGlassColors.MedicalGreen, "已完成")
        else -> Triple(LiquidGlassColors.GlassBg, colors.text400, status)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, style = ClearDuTypography.MedCardMeta, color = text)
    }
}