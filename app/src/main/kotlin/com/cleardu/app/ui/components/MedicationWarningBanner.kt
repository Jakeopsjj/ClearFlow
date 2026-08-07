package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors

/**
 * 用药不足警告横幅。
 *
 * 橙色玻璃材质横幅，左侧警告图标 + 文案，右侧"申请续药"按钮（按下 0.95 缩放）。
 */
@Composable
fun MedicationWarningBanner(
    mainText: String = "磷结合剂剩余3天用量",
    subText: String = "建议提前申请续药",
    onRefillClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.MedWarningRadius),
        background = LiquidGlassColors.LightTintOrangeBg,
        border = LiquidGlassColors.LightTintOrangeBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ClearDuDimens.MedWarningPaddingH,
                    vertical = ClearDuDimens.MedWarningPaddingV
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: icon + text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.MedWarningGap)
            ) {
                Box(
                    modifier = Modifier
                        .size(ClearDuDimens.MedWarningIconSize)
                        .clip(RoundedCornerShape(ClearDuDimens.MedWarningIconRadius))
                        .drawBehind {
                            drawRect(LiquidGlassColors.LightTintOrangeIcon)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    MedWarningTriangleIcon(
                        tint = LiquidGlassColors.MedicalOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = mainText,
                        style = ClearDuTypography.MedWarningMain,
                        color = LiquidGlassColors.MedicalOrange
                    )
                    Text(
                        text = subText,
                        style = ClearDuTypography.MedWarningSub,
                        color = LiquidGlassColors.LightTintOrangeText,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }
            // Right: refill button
            RefillButton(onClick = onRefillClick)
        }
    }
}

@Composable
private fun RefillButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        label = "refillBtnScale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(ClearDuDimens.MedWarningBtnPaddingH))
            .drawBehind {
                drawRect(LiquidGlassColors.LightTintOrangeBtn)
            }
            .border(
                BorderStroke(ClearDuDimens.GlassBorderWidth, LiquidGlassColors.LightTintOrangeBorder),
                RoundedCornerShape(ClearDuDimens.MedWarningBtnPaddingH)
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = ClearDuDimens.MedWarningBtnPaddingH,
                vertical = ClearDuDimens.MedWarningBtnPaddingV
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "申请续药",
            style = ClearDuTypography.MedRefillBtn,
            color = LiquidGlassColors.MedicalOrange
        )
    }
}
