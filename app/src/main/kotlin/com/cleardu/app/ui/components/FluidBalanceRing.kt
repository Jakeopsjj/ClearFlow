package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors

/**
 * Fluid balance ring progress chart.
 *
 * Reproduces the hero ring from the reference HTML:
 * - Circular progress with gradient stroke (cyan → blue)
 * - Center value (1,850ml) with target label
 * - Status dot with pulse animation
 * - Glow pulse animation on the ring
 */
@Composable
fun FluidBalanceRing(
    currentValue: Int,
    targetValue: Int,
    statusText: String,
    modifier: Modifier = Modifier
) {
    val progress = (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
    val circumference = (2 * Math.PI * 86).toFloat() // r=86

    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val smoothProgress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1500),
        label = "ringProgress"
    )

    LaunchedEffect(Unit) {
        animatedProgress = progress
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(ClearDuDimens.RingSize),
            contentAlignment = Alignment.Center
        ) {
            // Glow pulse behind the ring
            Box(
                modifier = Modifier
                    .size(ClearDuDimens.RingSize)
                    .drawBehind {
                        drawRingGlow(size)
                    }
            )

            val colors = backgroundAwareColors()
            val ringTrackColor = if (colors.isBright) LiquidGlassColors.BrightGlassBg else LiquidGlassColors.GlassBg

            Canvas(modifier = Modifier.size(ClearDuDimens.RingSize)) {
                val strokeWidth = ClearDuDimens.RingStrokeWidth.toPx()
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                val arcSize = Size(
                    size.width - strokeWidth,
                    size.height - strokeWidth
                )

                // Track (background ring) — 亮色背景用浅灰
                drawArc(
                    color = ringTrackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress (foreground ring with gradient)
                val sweepAngle = 360f * smoothProgress
                withTransform({
                    rotate(-90f, center)
                }) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                LiquidGlassColors.MedicalCyan,
                                LiquidGlassColors.MedicalBlue
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Center text
            val ringUnitColor = if (colors.isBright) colors.unitGreen else colors.text400
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    androidx.compose.material3.Text(
                        text = "%,d".format(currentValue),
                        style = ClearDuTypography.RingValue,
                        color = colors.foreground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(2.dp))
                    androidx.compose.material3.Text(
                        text = "ml",
                        style = ClearDuTypography.RingValueUnit,
                        color = ringUnitColor
                    )
                }
                Spacer(Modifier.height(ClearDuDimens.RingCenterGap))
                androidx.compose.material3.Text(
                    text = "今日目标 ${"%,d".format(targetValue)}ml",
                    style = ClearDuTypography.RingLabel,
                    color = ringUnitColor
                )
            }
        }

        // Status indicator
        Spacer(Modifier.height(ClearDuDimens.RingStatusTopMargin))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        drawCircle(
                            color = LiquidGlassColors.MedicalGreen,
                            radius = size.minDimension / 2f
                        )
                    }
            )
            Spacer(Modifier.width(6.dp))
            androidx.compose.material3.Text(
                text = statusText,
                style = ClearDuTypography.RingStatus,
                color = LiquidGlassColors.MedicalGreen
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRingGlow(
    canvasSize: Size
) {
    val brush = Brush.radialGradient(
        colors = listOf(
            LiquidGlassColors.TintCyanGlowStrong,
            LiquidGlassColors.TintCyanGlow,
            Color.Transparent
        ),
        center = Offset(canvasSize.width / 2f, canvasSize.height / 2f),
        radius = canvasSize.width / 2f
    )
    drawRect(brush = brush)
}
