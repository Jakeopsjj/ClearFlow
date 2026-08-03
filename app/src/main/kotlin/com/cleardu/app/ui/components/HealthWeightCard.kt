package com.cleardu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 体重记录卡片 — 健康数据页面。
 *
 * 展示当前体重、目标干体重、进度条和 7 天迷你趋势折线图。
 */
@Composable
fun HealthWeightCard(modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.HealthWeightRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = ClearDuDimens.HealthWeightPaddingH,
                    vertical = ClearDuDimens.HealthWeightPaddingV
                )
        ) {
            // 标题 + 迷你趋势图
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "体重记录",
                    style = ClearDuTypography.HealthCardTitle,
                    color = LiquidGlassColors.Foreground
                )
                MiniWeightChart(modifier = Modifier.height(ClearDuDimens.HealthMiniWeightHeight))
            }
            Spacer(Modifier.height(10.dp))

            // 当前体重 + 目标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "当前体重",
                        style = ClearDuTypography.HealthWeightLabel,
                        color = LiquidGlassColors.Text400
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "65.2",
                            style = ClearDuTypography.HealthWeightValue,
                            color = LiquidGlassColors.Foreground
                        )
                        Text(
                            text = " kg",
                            style = ClearDuTypography.HealthVitalUnit,
                            color = LiquidGlassColors.Text400
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "目标干体重",
                        style = ClearDuTypography.HealthWeightLabel,
                        color = LiquidGlassColors.Text400
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "63.0",
                            style = ClearDuTypography.HealthWeightValue,
                            color = LiquidGlassColors.MedicalCyan
                        )
                        Text(
                            text = " kg",
                            style = ClearDuTypography.HealthVitalUnit,
                            color = LiquidGlassColors.Text400
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // 进度条
            WeightProgressBar(modifier = Modifier.fillMaxWidth().height(ClearDuDimens.HealthWeightBarHeight))
            Spacer(Modifier.height(4.dp))

            // 刻度标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("68 kg", style = ClearDuTypography.HealthWeightScale, color = LiquidGlassColors.Text400)
                Text("目标 63 kg", style = ClearDuTypography.HealthWeightScale, color = LiquidGlassColors.MedicalGreen)
                Text("62 kg", style = ClearDuTypography.HealthWeightScale, color = LiquidGlassColors.Text400)
            }
        }
    }
}

/**
 * 体重进度条 — 渐变填充 + 目标标记。
 */
@Composable
private fun WeightProgressBar(modifier: Modifier = Modifier) {
    val fillRatio = 0.467f // 65.2 在 68~62 之间的位置
    val targetRatio = 0.833f // 63 在 68~62 之间的位置

    Box(modifier = modifier.clip(RoundedCornerShape(ClearDuDimens.HealthWeightBarRadius))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 轨道
            drawRect(LiquidGlassColors.TrackBg)
            // 填充
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(LiquidGlassColors.TintCyanBar, LiquidGlassColors.MedicalCyan)
                ),
                size = Size(size.width * fillRatio, size.height)
            )
        }
        // 目标标记线
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * targetRatio
            drawLine(
                color = LiquidGlassColors.TintGreenGlowStrong,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

/**
 * 迷你体重趋势折线图 — 青色填充 + 折线 + 末端圆点。
 */
@Composable
private fun MiniWeightChart(modifier: Modifier = Modifier) {
    val data = listOf(66.1f, 65.8f, 65.5f, 65.9f, 65.3f, 65.5f, 65.2f)

    Canvas(modifier = modifier) {
        val max = data.max()
        val min = data.min()
        val range = (max - min).coerceAtLeast(1f)
        val stepX = size.width / (data.size - 1)

        val points = data.mapIndexed { i, v ->
            Offset(
                x = i * stepX,
                y = size.height - ((v - min) / range) * size.height * 0.7f - size.height * 0.15f
            )
        }

        // 填充区域
        val fillPath = Path().apply {
            moveTo(points.first().x, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    LiquidGlassColors.TintCyanGlow.copy(alpha = 0.4f),
                    Color.Transparent
                )
            )
        )

        // 折线
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.forEachIndexed { i, p -> if (i > 0) lineTo(p.x, p.y) }
        }
        drawPath(
            path = linePath,
            color = LiquidGlassColors.MedicalCyan,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 末端圆点
        drawCircle(
            color = LiquidGlassColors.MedicalCyan,
            radius = 3.5.dp.toPx(),
            center = points.last()
        )
        drawCircle(
            color = Color.White,
            radius = 3.5.dp.toPx(),
            center = points.last(),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}
