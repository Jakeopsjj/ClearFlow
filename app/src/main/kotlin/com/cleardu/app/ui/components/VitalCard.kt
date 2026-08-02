package com.cleardu.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.data.VitalItem
import com.cleardu.app.data.VitalStatus
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuMotion
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * A single vital sign card in the 2x2 dashboard grid.
 *
 * Reproduces `.vital-card` from the reference:
 * - Glass background with rounded corners
 * - Icon + status tag header
 * - Large monospace value with unit
 * - Optional sub-line (e.g., weight change)
 * - Press scale animation
 */
@Composable
fun VitalCard(
    item: VitalItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (pressed) ClearDuMotion.CardPressScale else 1f
                scaleY = if (pressed) ClearDuMotion.CardPressScale else 1f
            }
            .clip(RoundedCornerShape(ClearDuDimens.VitalCardRadius))
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(
                    bounded = true,
                    color = Color.White
                ),
                onClick = onClick
            ),
        shape = RoundedCornerShape(ClearDuDimens.VitalCardRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(
            modifier = Modifier.padding(ClearDuDimens.VitalCardPadding)
        ) {
            // Header: icon + status tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VitalIcon(accentColor = item.accentColor)
                if (item.status == VitalStatus.Normal) {
                    VitalStatusTag(text = "正常")
                }
            }

            Spacer(Modifier.height(ClearDuDimens.VitalHeaderBottomMargin))

            // Value row
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = item.value,
                    style = ClearDuTypography.VitalValue,
                    color = LiquidGlassColors.Foreground,
                    textAlign = TextAlign.Start
                )
                if (item.unit.isNotEmpty()) {
                    Spacer(Modifier.size(3.dp))
                    Text(
                        text = item.unit,
                        style = ClearDuTypography.VitalUnit,
                        color = LiquidGlassColors.Text400
                    )
                }
            }

            // Sub value
            item.subValue?.let { sub ->
                Spacer(Modifier.height(ClearDuDimens.VitalSubTopMargin))
                Text(
                    text = sub,
                    style = ClearDuTypography.VitalSub,
                    color = LiquidGlassColors.Text400
                )
            }
        }
    }
}

@Composable
private fun VitalIcon(accentColor: Color) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(ClearDuDimens.VitalIconSize)
            .clip(RoundedCornerShape(ClearDuDimens.VitalIconRadius))
            .drawBehindFill(LiquidGlassColors.GlassBgLight),
        contentAlignment = Alignment.Center
    ) {
        // Simple circle dot as icon placeholder
        androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
            drawCircle(color = accentColor, radius = 6.dp.toPx())
        }
    }
}

@Composable
private fun VitalStatusTag(text: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .drawBehindFill(LiquidGlassColors.TintGreenStrong)
            .padding(
                horizontal = ClearDuDimens.VitalStatusTagPaddingH,
                vertical = ClearDuDimens.VitalStatusTagPaddingV
            )
    ) {
        Text(
            text = text,
            style = ClearDuTypography.VitalStatusTag,
            color = LiquidGlassColors.MedicalGreen
        )
    }
}

private fun Modifier.drawBehindFill(color: Color): Modifier =
    this.then(
        Modifier.drawBehind {
            drawRect(color = color)
        }
    )
