package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 警告横幅 — 健康数据页面。
 *
 * 橙色玻璃材质横幅，显示血磷偏高提示和"查看建议"按钮。
 */
@Composable
fun HealthWarningBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ClearDuDimens.HealthWarningRadius))
            .drawBehind {
                drawRect(LiquidGlassColors.TintOrangeBg)
            }
            .drawBehind {
                // 外发光
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidGlassColors.TintOrangeGlow.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = size.maxDimension
                    )
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ClearDuDimens.HealthWarningPaddingH,
                    vertical = ClearDuDimens.HealthWarningPaddingV
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 警告文本
            Text(
                text = "血磷偏高，建议减少高磷食物摄入",
                style = ClearDuTypography.HealthWarningText,
                color = LiquidGlassColors.Text100,
                modifier = Modifier.weight(1f)
            )
            // 查看建议按钮
            WarningButton(onClick = onClick)
        }
    }
}

@Composable
private fun WarningButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        label = "warningBtnScale"
    )

    Row(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .drawBehind {
                drawRect(LiquidGlassColors.TintOrangeActive.copy(alpha = 0.3f))
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = ClearDuDimens.HealthWarningBtnPaddingH,
                vertical = ClearDuDimens.HealthWarningBtnPaddingV
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "查看建议",
            style = ClearDuTypography.HealthWarningBtn,
            color = LiquidGlassColors.MedicalOrange
        )
    }
}

/**
 * 导出/分享按钮行 — 健康数据页面底部。
 *
 * 两个等宽玻璃按钮：导出报告、分享给医生。
 */
@Composable
fun HealthExportButtons(
    onExport: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.HealthExportGap)
    ) {
        ExportButton(
            text = "导出报告",
            onClick = onExport,
            modifier = Modifier.weight(1f)
        )
        ExportButton(
            text = "分享给医生",
            onClick = onShare,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExportButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        label = "exportBtnScale"
    )

    GlassCard(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(ClearDuDimens.HealthExportRadius))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(ClearDuDimens.HealthExportRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = ClearDuDimens.HealthExportPaddingH,
                    vertical = ClearDuDimens.HealthExportPaddingV
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 图标占位 — 青色圆点
            Box(
                modifier = Modifier
                    .size(ClearDuDimens.HealthExportIconSize)
                    .drawBehind {
                        drawCircle(LiquidGlassColors.MedicalCyan.copy(alpha = 0.3f))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (text == "导出报告") "↑" else "↗",
                    color = LiquidGlassColors.MedicalCyan,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = text,
                style = ClearDuTypography.HealthExportBtn,
                color = LiquidGlassColors.Foreground
            )
        }
    }
}
