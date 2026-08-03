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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import java.util.Locale

/**
 * "体重体温" tab content for the data-record page.
 *
 * Two equal-width glass cards in a row — weight (kg) and temperature (°C) — each
 * showing a label, a large monospace value, a unit, and a pair of circular
 * glass adjust buttons that nudge the value by ±0.1. Buttons scale to 0.88 and
 * tint to [LiquidGlassColors.TintCyanStrong] on press.
 *
 * @param modifier outer modifier
 */
@Composable
fun WeightTempPanel(modifier: Modifier = Modifier) {
    var weight by remember { mutableStateOf(65.2) }
    var temp by remember { mutableStateOf(36.5) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.WtGridGap)
    ) {
        WeightTempCard(
            modifier = Modifier.weight(1f),
            label = "体重",
            value = String.format(Locale.US, "%.1f", weight),
            unit = "kg",
            onDecrease = { weight = roundTo1Decimal(weight - 0.1) },
            onIncrease = { weight = roundTo1Decimal(weight + 0.1) }
        )
        WeightTempCard(
            modifier = Modifier.weight(1f),
            label = "体温",
            value = String.format(Locale.US, "%.1f", temp),
            unit = "°C",
            onDecrease = { temp = roundTo1Decimal(temp - 0.1) },
            onIncrease = { temp = roundTo1Decimal(temp + 0.1) }
        )
    }
}

@Composable
private fun WeightTempCard(
    label: String,
    value: String,
    unit: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.heightIn(min = ClearDuDimens.WtCardMinHeight),
        shape = RoundedCornerShape(ClearDuDimens.WtCardRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = ClearDuDimens.WtCardPaddingH,
                    vertical = ClearDuDimens.WtCardPaddingV
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = ClearDuTypography.BpCardLabel,
                color = LiquidGlassColors.Text400,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = ClearDuTypography.WtCardValue,
                color = LiquidGlassColors.Foreground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = unit,
                style = ClearDuTypography.BpCardUnit,
                color = LiquidGlassColors.Text400,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(ClearDuDimens.BpAdjustTopMargin))
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.BpAdjustGap)
            ) {
                AdjustButton(text = "-", onClick = onDecrease)
                AdjustButton(text = "+", onClick = onIncrease)
            }
        }
    }
}

/**
 * A circular glass adjust button. Scales to 0.88 and fills with
 * [LiquidGlassColors.TintCyanStrong] while pressed.
 */
@Composable
private fun AdjustButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        label = "wtAdjustScale"
    )

    GlassCard(
        modifier = modifier
            .size(ClearDuDimens.BpAdjustBtnSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        background = if (pressed) LiquidGlassColors.TintCyanStrong else LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = ClearDuTypography.KeyActionText.copy(fontWeight = FontWeight.W600),
                color = LiquidGlassColors.MedicalCyan
            )
        }
    }
}

/**
 * Rounds a double to 1 decimal place, avoiding floating-point drift when the
 * user repeatedly taps ±0.1 (e.g. 65.2 - 0.1 = 65.10000000000001).
 */
private fun roundTo1Decimal(value: Double): Double =
    Math.round(value * 10.0) / 10.0