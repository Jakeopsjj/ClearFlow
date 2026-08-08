package com.cleardu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors

/**
 * 超滤量趋势图卡片 — 健康数据页面顶部。
 *
 * 液态玻璃卡片内绘制 7 天折线 + 渐变填充面积图，
 * 含 Y 轴刻度、网格虚线、目标线和末端脉冲数据点。
 *
 * @param data 数据列表（如 7 天），单位 ml
 * @param target 目标线值
 * @param averageValue 均值
 * @param complianceDays 达标天数
 * @param totalDays 总天数
 * @param modifier 外部 modifier
 */
@Composable
fun HealthUfTrendCard(
    data: List<Float>,
    target: Float,
    averageValue: Int = 0,
    complianceDays: Int = 0,
    totalDays: Int = 7,
    modifier: Modifier = Modifier
) {
    val colors = backgroundAwareColors()
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.HealthChartCardRadius),
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = ClearDuDimens.HealthChartCardPaddingH,
                    vertical = ClearDuDimens.HealthChartCardPaddingV
                )
        ) {
            // 标题 + 副标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "超滤量趋势",
                    style = ClearDuTypography.HealthCardTitle,
                    color = colors.foreground
                )
                Text(
                    text = "最近${totalDays}天",
                    style = ClearDuTypography.HealthCardSubtitle,
                    color = colors.text400
                )
            }
            Spacer(Modifier.height(8.dp))

            // 趋势图
            UfTrendChart(
                data = data,
                target = target,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ClearDuDimens.HealthChartHeight)
            )

            Spacer(Modifier.height(ClearDuDimens.HealthChartComplianceTopMargin))

            // 平均值 + 达标率
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "本周平均",
                        style = ClearDuTypography.HealthAvgLabel,
                        color = colors.text400
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = if (averageValue > 0) "${averageValue.toFloat() / 1f} ml/天" else "-- ml/天",
                        style = ClearDuTypography.HealthAvgValue,
                        color = LiquidGlassColors.MedicalCyan
                    )
                }
                // 达标率圆点 + 文字
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(
                        modifier = Modifier
                            .size(6.dp)
                            .drawBehind {
                                drawCircle(LiquidGlassColors.MedicalGreen)
                            }
                    ) {}
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "达标率: $complianceDays/$totalDays 天",
                        style = ClearDuTypography.HealthComplianceText,
                        color = colors.text400
                    )
                }
            }
        }
    }
}

/**
 * 超滤量趋势折线图（Canvas 原生绘制）。
 */
@Composable
private fun UfTrendChart(
    data: List<Float>,
    target: Float,
    modifier: Modifier = Modifier
) {
    val colors = backgroundAwareColors()
    val maxScale = 2500f
    val yLabels = listOf("2500", "2000", "1500", "1000")
    val xLabels = listOf("一", "二", "三", "四", "五", "六", "日")
    val lineColor = LiquidGlassColors.MedicalCyan
    val gridColor = LiquidGlassColors.ChartGrid
    val targetColor = LiquidGlassColors.ChartTargetLine

    Canvas(modifier = modifier) {
        val chartW = size.width
        val chartH = size.height
        val axisLabelW = 36.dp.toPx()
        val chartLeft = axisLabelW
        val chartRight = chartW
        val chartTop = 4.dp.toPx()
        val chartBottom = chartH - 20.dp.toPx()
        val chartDrawW = chartRight - chartLeft
        val chartDrawH = chartBottom - chartTop

        // 网格虚线
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
        yLabels.indices.forEach { i ->
            val y = chartTop + chartDrawH * (i.toFloat() / (yLabels.size - 1))
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )
        }

        // 目标线
        val targetY = chartBottom - (target / maxScale) * chartDrawH
        drawLine(
            color = targetColor,
            start = Offset(chartLeft, targetY),
            end = Offset(chartRight, targetY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
        )

        if (data.isEmpty()) return@Canvas

        // 计算数据点坐标
        val stepX = if (data.size > 1) chartDrawW / (data.size - 1) else chartDrawW
        val points = data.mapIndexed { i, v ->
            Offset(
                x = chartLeft + i * stepX,
                y = chartBottom - (v / maxScale).coerceIn(0f, 1f) * chartDrawH
            )
        }

        // 渐变填充区域
        val fillPath = Path().apply {
            moveTo(points.first().x, chartBottom)
            points.forEach { p -> lineTo(p.x, p.y) }
            lineTo(points.last().x, chartBottom)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(LiquidGlassColors.ChartFillStart, LiquidGlassColors.ChartFillEnd),
                startY = chartTop,
                endY = chartBottom
            )
        )

        // 折线
        if (points.size > 1) {
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.forEachIndexed { i, p -> if (i > 0) lineTo(p.x, p.y) }
            }
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // 数据点
        points.forEachIndexed { i, p ->
            val r = if (i == points.lastIndex) 4.5.dp.toPx() else 4.dp.toPx()
            drawCircle(color = lineColor, radius = r, center = p)
            drawCircle(color = Color.White, radius = r, center = p, style = Stroke(width = 1.5.dp.toPx()))
        }
    }

    // X 轴标签
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        xLabels.forEach { label ->
            Text(
                text = label,
                style = ClearDuTypography.HealthChartAxis,
                color = colors.text400,
                textAlign = TextAlign.Center
            )
        }
    }
}