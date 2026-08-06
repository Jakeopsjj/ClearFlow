package com.cleardu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 电解质 2×2 网格 — 健康数据页面。
 *
 * 四张液态玻璃卡片：钾(K)、磷(P)、钠(Na)、钙(Ca)。
 * 每张含元素符号、名称、数值、单位、范围条和范围标签。
 *
 * @param potassium 钾值 (mmol/L)
 * @param phosphorus 磷值 (mmol/L)
 * @param sodium 钠值 (mmol/L)
 * @param calcium 钙值 (mmol/L)
 * @param modifier 外部 modifier
 */
@Composable
fun HealthElectrolyteGrid(
    potassium: Double = 0.0,
    phosphorus: Double = 0.0,
    sodium: Double = 0.0,
    calcium: Double = 0.0,
    modifier: Modifier = Modifier
) {
    fun fmt(v: Double, default: String): String = if (v > 0) "${v}" else default

    val elements = listOf(
        ElectrolyteItem("K", "钾", fmt(potassium, "4.2"), "mmol/L", 3.5f, 5.5f, potassium.toFloat().coerceIn(3.0f, 6.0f), isWarning = potassium > 5.5),
        ElectrolyteItem("P", "磷", fmt(phosphorus, "1.8"), "mmol/L", 0.8f, 1.6f, phosphorus.toFloat().coerceIn(0.5f, 2.2f), isWarning = phosphorus > 1.6),
        ElectrolyteItem("Na", "钠", fmt(sodium, "138"), "mmol/L", 135f, 145f, sodium.toFloat().coerceIn(130f, 150f), isWarning = sodium < 135 || sodium > 145),
        ElectrolyteItem("Ca", "钙", fmt(calcium, "2.3"), "mmol/L", 2.1f, 2.6f, calcium.toFloat().coerceIn(1.8f, 3.0f), isWarning = calcium < 2.1 || calcium > 2.6)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ClearDuDimens.HealthEleGridGap)
    ) {
        elements.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.HealthEleGridGap)
            ) {
                rowItems.forEach { item ->
                    ElectrolyteCard(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size < 2) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

private data class ElectrolyteItem(
    val symbol: String,
    val name: String,
    val value: String,
    val unit: String,
    val rangeMin: Float,
    val rangeMax: Float,
    val currentValue: Float,
    val isWarning: Boolean
)

@Composable
private fun ElectrolyteCard(item: ElectrolyteItem, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.HealthEleCardRadius),
        background = LiquidGlassColors.GlassBg,
        border = if (item.isWarning) LiquidGlassColors.TintOrangeBorder else LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = ClearDuDimens.HealthEleCardPaddingH,
                    vertical = ClearDuDimens.HealthEleCardPaddingV
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.symbol,
                style = ClearDuTypography.HealthEleSymbol,
                color = LiquidGlassColors.MedicalCyan,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.name,
                style = ClearDuTypography.HealthEleName,
                color = LiquidGlassColors.Text400,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))

            val valueColor = if (item.isWarning) LiquidGlassColors.MedicalOrange else LiquidGlassColors.Foreground
            Text(
                text = item.value,
                style = ClearDuTypography.HealthEleValue,
                color = valueColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.unit,
                style = ClearDuTypography.HealthEleUnit,
                color = LiquidGlassColors.Text400,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            ElectrolyteRangeBar(
                min = item.rangeMin,
                max = item.rangeMax,
                current = item.currentValue,
                isWarning = item.isWarning,
                modifier = Modifier.fillMaxWidth().height(ClearDuDimens.HealthEleRangeBarHeight)
            )
            Spacer(Modifier.height(4.dp))

            Text(
                text = "${item.rangeMin} / ${item.rangeMax}",
                style = ClearDuTypography.HealthEleRange,
                color = if (item.isWarning) LiquidGlassColors.MedicalOrange else LiquidGlassColors.Text400,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 电解质范围条 — 正常区绿色渐变，警告区橙色渐变，圆形指示器标记当前位置。
 */
@Composable
private fun ElectrolyteRangeBar(
    min: Float,
    max: Float,
    current: Float,
    isWarning: Boolean,
    modifier: Modifier = Modifier
) {
    val indicatorColor = if (isWarning) LiquidGlassColors.MedicalOrange else LiquidGlassColors.White
    val indicatorGlow = if (isWarning) LiquidGlassColors.TintOrangeActive else LiquidGlassColors.TintGreenGlow

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
        ) {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(LiquidGlassColors.TintGreenBarStart, LiquidGlassColors.TintGreenBarEnd)
                )
            )
            if (isWarning) {
                drawRect(
                    color = LiquidGlassColors.TintOrangeBarStart,
                    topLeft = Offset(size.width * 0.8f, 0f),
                    size = Size(size.width * 0.2f, size.height)
                )
            }
        }

        val ratio = ((current - min) / (max - min)).coerceIn(0f, 1f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = size.width * ratio
            val cy = size.height / 2f
            drawCircle(color = indicatorGlow.copy(alpha = 0.5f), radius = 6.dp.toPx(), center = Offset(x, cy))
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(x, cy))
            drawCircle(color = indicatorColor, radius = 3.dp.toPx(), center = Offset(x, cy))
        }
    }
}