package com.cleardu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 血压 & 心率双卡片 — 健康数据页面。
 *
 * 左右两个液态玻璃卡片，分别展示血压（含迷你柱状图）和心率（含迷你折线图）。
 */
@Composable
fun HealthBpHrCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.HealthBpHrGap)
    ) {
        // 血压卡片
        BpSection(modifier = Modifier.weight(1f))
        // 心率卡片
        HrSection(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BpSection(modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(ClearDuDimens.HealthBpHrRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = ClearDuDimens.HealthBpHrPaddingH,
                    vertical = ClearDuDimens.HealthBpHrPaddingV
                )
        ) {
            // 标签
            Text(
                text = "血压",
                style = ClearDuTypography.HealthVitalLabel,
                color = LiquidGlassColors.Text400
            )
            Spacer(Modifier.height(4.dp))

            // 数值 + 单位
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "128/82",
                    style = ClearDuTypography.HealthBpValue,
                    color = LiquidGlassColors.Foreground
                )
                Text(
                    text = "mmHg",
                    style = ClearDuTypography.HealthVitalUnit,
                    color = LiquidGlassColors.Text400
                )
            }
            Spacer(Modifier.height(6.dp))

            // 状态标签
            StatusTag(
                text = "血压正常",
                dotColor = LiquidGlassColors.BpNormal,
                textColor = LiquidGlassColors.MedicalGreen
            )
            Spacer(Modifier.height(8.dp))

            // 迷你柱状图
            MiniBarChart(modifier = Modifier.fillMaxWidth().height(ClearDuDimens.HealthMiniBarHeight))
        }
    }
}

@Composable
private fun HrSection(modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(ClearDuDimens.HealthBpHrRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = ClearDuDimens.HealthBpHrPaddingH,
                    vertical = ClearDuDimens.HealthBpHrPaddingV
                )
        ) {
            // 标签
            Text(
                text = "心率",
                style = ClearDuTypography.HealthVitalLabel,
                color = LiquidGlassColors.Text400
            )
            Spacer(Modifier.height(4.dp))

            // 数值 + 单位
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "72",
                    style = ClearDuTypography.HealthHrValue,
                    color = LiquidGlassColors.Foreground
                )
                Text(
                    text = "bpm",
                    style = ClearDuTypography.HealthVitalUnit,
                    color = LiquidGlassColors.Text400
                )
            }
            Spacer(Modifier.height(6.dp))

            // 状态标签
            StatusTag(
                text = "心率正常",
                dotColor = LiquidGlassColors.HrNormal,
                textColor = LiquidGlassColors.MedicalGreen
            )
            Spacer(Modifier.height(8.dp))

            // 迷你折线图
            MiniLineChart(modifier = Modifier.fillMaxWidth().height(ClearDuDimens.HealthMiniLineHeight))
        }
    }
}

/**
 * 状态标签 — 胶囊形带前置圆点。
 */
@Composable
fun StatusTag(
    text: String,
    dotColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .drawBehind {
                drawRect(dotColor.copy(alpha = 0.15f))
            }
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(5.dp).drawBehind { drawCircle(dotColor) }) {}
        Text(
            text = text,
            style = ClearDuTypography.HealthStatusTag,
            color = textColor
        )
    }
}

/**
 * 迷你柱状图 — 7 根渐变柱。
 */
@Composable
private fun MiniBarChart(modifier: Modifier = Modifier) {
    val heights = listOf(0.55f, 0.65f, 0.70f, 0.60f, 0.75f, 0.80f, 0.68f)
    Canvas(modifier = modifier) {
        val barW = size.width / heights.size * 0.7f
        val gap = size.width / heights.size * 0.3f
        val cornerR = 3.dp.toPx()

        heights.forEachIndexed { i, h ->
            val x = i * (barW + gap) + gap / 2f
            val barH = size.height * h
            val y = size.height - barH
            val isLast = i == heights.lastIndex

            drawRoundRect(
                color = if (isLast) LiquidGlassColors.TintCyanGlow else LiquidGlassColors.TintCyanBar,
                topLeft = Offset(x, y),
                size = Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerR, cornerR)
            )
        }
    }
}

/**
 * 迷你折线图 — 渐变折线 + 末端圆点。
 */
@Composable
private fun MiniLineChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val points = listOf(
            Offset(size.width * 0f, size.height * 0.6f),
            Offset(size.width * 0.2f, size.height * 0.4f),
            Offset(size.width * 0.4f, size.height * 0.7f),
            Offset(size.width * 0.55f, size.height * 0.3f),
            Offset(size.width * 0.7f, size.height * 0.5f),
            Offset(size.width * 0.85f, size.height * 0.35f),
            Offset(size.width * 1f, size.height * 0.45f)
        )

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(LiquidGlassColors.MedicalRed, LiquidGlassColors.MedicalPink)
            ),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 末端圆点
        drawCircle(
            color = LiquidGlassColors.MedicalRed,
            radius = 3.dp.toPx(),
            center = points.last()
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = points.last(),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}
