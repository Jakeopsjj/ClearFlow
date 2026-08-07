package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.cleardu.app.ui.theme.ClearDuMotion
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.glassParams

/**
 * 用药提醒设置入口卡片。
 *
 * 紫色图标 + 标题 + 右侧箭头，整卡可点击（按下 0.98 缩放）。
 */
@Composable
fun MedicationSettingsEntry(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) ClearDuMotion.CardPressScale else 1f,
        label = "settingsEntryScale"
    )
    val glass = glassParams()

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(ClearDuDimens.MedSettingsRadius))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(ClearDuDimens.MedSettingsRadius),
        background = glass.background,
        border = glass.border,
        shadowColor = glass.shadowColor,
        shadowElevation = glass.shadowElevation,
        specularTop = glass.specularTop
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ClearDuDimens.MedSettingsPaddingH,
                    vertical = ClearDuDimens.MedSettingsPaddingV
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(ClearDuDimens.MedSettingsIconSize)
                        .clip(RoundedCornerShape(ClearDuDimens.MedSettingsIconRadius))
                        .drawBehind { drawRect(LiquidGlassColors.TintPurpleBg) },
                    contentAlignment = Alignment.Center
                ) {
                    MedGearIcon(
                        tint = LiquidGlassColors.MedicalPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "用药提醒设置",
                    style = ClearDuTypography.MedSettingsText,
                    color = LiquidGlassColors.Foreground
                )
            }
            MedChevronRightIcon(
                tint = LiquidGlassColors.Text400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
