package com.cleardu.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.cleardu.app.ui.theme.glassParams

/**
 * 今日服药进度玻璃卡片。
 *
 * 左侧 72dp 圆环显示 75% 完成度（带渐变描边 + 呼吸光晕），右侧文字列展示
 * 进度详情与下次服药提示。
 */
@Composable
fun MedicationProgressCard(
    takenCount: Int = 3,
    totalCount: Int = 4,
    nextMedName: String = "降压药",
    nextMedTime: String = "14:00",
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount > 0) {
        (takenCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val pctText = "${(progress * 100).toInt()}%"
    val glass = glassParams(lightMode = true)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.MedProgressCardRadius),
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
                    horizontal = ClearDuDimens.MedProgressCardPaddingH,
                    vertical = ClearDuDimens.MedProgressCardPaddingV
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.MedProgressCardGap)
        ) {
            ProgressRing(progress = progress, percentText = pctText)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "今日服药进度",
                    style = ClearDuTypography.MedProgressTitle,
                    color = LiquidGlassColors.LightForeground
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "已服 $takenCount / 共 $totalCount 次",
                    style = ClearDuTypography.MedProgressDetail,
                    color = LiquidGlassColors.Text400
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MedClockIcon(
                        tint = LiquidGlassColors.MedicalCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "下次: $nextMedName $nextMedTime",
                        style = ClearDuTypography.MedProgressNext,
                        color = LiquidGlassColors.MedicalCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, percentText: String) {
    // Animated fill (1.5s iOS-style ease)
    var target by remember { mutableFloatStateOf(0f) }
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "medProgressFill"
    )
    LaunchedEffect(progress) { target = progress }

    // Breathing glow (2.5s alpha pulse 0.15 -> 0.35)
    val glowTransition = rememberInfiniteTransition(label = "medProgressGlow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "medProgressGlowAlpha"
    )

    Box(
        modifier = Modifier.size(ClearDuDimens.MedProgressRingSize),
        contentAlignment = Alignment.Center
    ) {
        // Breathing glow behind the ring
        Box(
            modifier = Modifier
                .size(ClearDuDimens.MedProgressRingSize)
                .drawBehind {
                    val brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidGlassColors.MedicalBlue.copy(alpha = glowAlpha),
                            LiquidGlassColors.MedicalBlue.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.width / 2f
                    )
                    drawRect(brush = brush)
                }
        )

        // Track + progress arc
        Canvas(modifier = Modifier.size(ClearDuDimens.MedProgressRingSize)) {
            val strokeWidth = ClearDuDimens.MedProgressRingStrokeWidth.toPx()
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            // Track
            drawArc(
                color = LiquidGlassColors.LightGlassBg,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress (MedicalBlue gradient)
            val sweep = 360f * animated
            withTransform({ rotate(-90f, center) }) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            LiquidGlassColors.MedicalBlue,
                            LiquidGlassColors.MedicalCyan
                        )
                    ),
                    startAngle = 0f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center percentage
        Text(
            text = percentText,
            style = ClearDuTypography.MedProgressPct,
            color = LiquidGlassColors.LightForeground,
            textAlign = TextAlign.Center
        )
    }
}
