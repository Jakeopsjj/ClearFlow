package com.cleardu.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Liquid-glass segmented control for the data-record page.
 *
 * Reproduces `.segmented-control.glass` from the reference HTML:
 * - Translucent glass track with 14dp radius, 3dp inner padding
 * - Each tab is a pill with an 11dp radius
 * - Active tab fills with a cyan→blue gradient and tints text MedicalCyan;
 *   inactive tabs use Text600
 * - 5 tab labels: 超滤量 / 血压心率 / 体重体温 / 元素检测 / 用药
 *
 * @param tabs tab labels
 * @param selectedIndex currently active tab index
 * @param onSelected callback when a tab is tapped
 */
@Composable
fun SegmentedControl(
    tabs: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.SegControlRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClearDuDimens.SegControlPadding),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            tabs.forEachIndexed { index, label ->
                SegItem(
                    label = label,
                    isActive = index == selectedIndex,
                    onClick = { onSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SegItem(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isActive) LiquidGlassColors.MedicalCyan else LiquidGlassColors.SegInactiveText

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ClearDuDimens.SegIndicatorRadius))
            .then(
                if (isActive) {
                    Modifier.drawBehind {
                        drawRect(
                            Brush.linearGradient(
                                colors = listOf(
                                    LiquidGlassColors.SegIndicatorStart,
                                    LiquidGlassColors.SegIndicatorEnd
                                )
                            )
                        )
                    }
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(
                vertical = ClearDuDimens.SegItemPaddingV,
                horizontal = ClearDuDimens.SegItemPaddingH
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = ClearDuTypography.SegItem,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
