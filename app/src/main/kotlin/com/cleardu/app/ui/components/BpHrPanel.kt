package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * "血压心率" tab content for the data-record page.
 *
 * Layout:
 * 1. BP Grid — two equal-width glass cards side by side
 *    (收缩压 / 舒张压), each with label, value, unit, and ± adjust buttons
 * 2. HR Card — full-width glass card with heart icon on the left,
 *    heart rate value in the center, and ± adjust buttons on the right
 *
 * @param modifier outer modifier
 */
@Composable
fun BpHrPanel(modifier: Modifier = Modifier) {
    var systolic by remember { mutableIntStateOf(120) }
    var diastolic by remember { mutableIntStateOf(80) }
    var heartRate by remember { mutableIntStateOf(75) }

    Column(modifier = modifier.fillMaxWidth()) {
        // === BP Grid ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.BpGridGap)
        ) {
            BpCard(
                label = "收缩压",
                value = systolic.toString(),
                unit = "mmHg",
                onDecrease = { systolic = (systolic - 5).coerceAtLeast(0) },
                onIncrease = { systolic = (systolic + 5) },
                modifier = Modifier.weight(1f)
            )
            BpCard(
                label = "舒张压",
                value = diastolic.toString(),
                unit = "mmHg",
                onDecrease = { diastolic = (diastolic - 5).coerceAtLeast(0) },
                onIncrease = { diastolic = (diastolic + 5) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(ClearDuDimens.BpCardBottomMargin))

        // === HR Card ===
        HrCard(
            value = heartRate.toString(),
            onDecrease = { heartRate = (heartRate - 1).coerceAtLeast(0) },
            onIncrease = { heartRate = (heartRate + 1) }
        )
    }
}

// ===== BP Card =====

@Composable
private fun BpCard(
    label: String,
    value: String,
    unit: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.heightIn(min = ClearDuDimens.BpCardMinHeight),
        shape = RoundedCornerShape(ClearDuDimens.BpCardRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = ClearDuDimens.BpCardPaddingH,
                    vertical = ClearDuDimens.BpCardPaddingV
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
                style = ClearDuTypography.BpCardValue,
                color = LiquidGlassColors.Foreground,
                textAlign = TextAlign.Center
            )
            Text(
                text = unit,
                style = ClearDuTypography.BpCardUnit,
                color = LiquidGlassColors.Text400,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(ClearDuDimens.BpAdjustTopMargin))
            Row(horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.BpAdjustGap)) {
                BpAdjustButton(text = "-", onClick = onDecrease)
                BpAdjustButton(text = "+", onClick = onIncrease)
            }
        }
    }
}

// ===== HR Card =====

@Composable
private fun HrCard(
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.HrCardRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ClearDuDimens.HrCardPaddingH,
                    vertical = ClearDuDimens.HrCardPaddingV
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: heart icon + value
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.HrInfoGap)
            ) {
                Box(
                    modifier = Modifier
                        .size(ClearDuDimens.HrIconSize)
                        .clip(RoundedCornerShape(ClearDuDimens.HrIconRadius))
                        .drawBehind {
                            drawRect(LiquidGlassColors.TintRedBg)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    HeartIcon()
                }
                Column {
                    Text(
                        text = "心率",
                        style = ClearDuTypography.BpCardLabel,
                        color = LiquidGlassColors.Text400
                    )
                    Text(
                        text = value,
                        style = ClearDuTypography.HrInfoValue,
                        color = LiquidGlassColors.Foreground
                    )
                    Text(
                        text = "bpm",
                        style = ClearDuTypography.BpCardUnit,
                        color = LiquidGlassColors.Text400
                    )
                }
            }

            // Right: adjust buttons
            Row(horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.HrAdjustGap)) {
                BpAdjustButton(text = "-", onClick = onDecrease)
                BpAdjustButton(text = "+", onClick = onIncrease)
            }
        }
    }
}

// ===== Shared adjust button =====

@Composable
private fun BpAdjustButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        label = "bpAdjustScale"
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
                style = ClearDuTypography.KeyActionText,
                color = LiquidGlassColors.MedicalCyan
            )
        }
    }
}

// ===== Heart icon (20x20 viewBox) =====

@Composable
private fun HeartIcon() {
    Canvas(modifier = Modifier.size(18.dp)) {
        val s = size.width / 20f
        val fill = LiquidGlassColors.MedicalRed.copy(alpha = 0.9f)
        val stroke = LiquidGlassColors.White
        val strokeW = 1f * s

        // Heart shape
        val heartPath = Path().apply {
            moveTo(10f * s, 17f * s)
            cubicTo(10f * s, 17f * s, 4f * s, 13f * s, 4f * s, 8f * s)
            cubicTo(4f * s, 5.5f * s, 5.8f * s, 4f * s, 7.5f * s, 4f * s)
            cubicTo(8.8f * s, 4f * s, 9.6f * s, 4.8f * s, 10f * s, 5.6f * s)
            cubicTo(10.4f * s, 4.8f * s, 11.2f * s, 4f * s, 12.5f * s, 4f * s)
            cubicTo(14.2f * s, 4f * s, 16f * s, 5.5f * s, 16f * s, 8f * s)
            cubicTo(16f * s, 13f * s, 10f * s, 17f * s, 10f * s, 17f * s)
            close()
        }
        drawPath(heartPath, fill)

        // ECG line inside heart
        val ecgPath = Path().apply {
            moveTo(7f * s, 10f * s)
            lineTo(9f * s, 10f * s)
            lineTo(10f * s, 8f * s)
            lineTo(11f * s, 11.5f * s)
            lineTo(12f * s, 10f * s)
            lineTo(14f * s, 10f * s)
        }
        drawPath(
            ecgPath,
            stroke,
            style = Stroke(
                width = strokeW,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}