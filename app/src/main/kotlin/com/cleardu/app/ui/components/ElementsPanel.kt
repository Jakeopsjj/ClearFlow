package com.cleardu.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * "元素检测" tab content for the data-record page.
 *
 * A 2x2 grid of glass cards for the four dialysis-critical electrolytes
 * (K / P / Na / Ca). Each card shows the element symbol, Chinese name,
 * current value, unit, and a green "正常 x-y" range badge. Because the panel
 * lives inside a vertically scrollable parent, the grid is built with
 * [Column] + [chunked] rows (mirroring the VitalsGrid pattern) rather than a
 * nested scroll container.
 *
 * @param modifier outer modifier
 */
@Composable
fun ElementsPanel(modifier: Modifier = Modifier) {
    val elements = listOf(
        ElementItem("K", "钾", "4.2", "mmol/L", "正常 3.5-5.5"),
        ElementItem("P", "磷", "1.5", "mmol/L", "正常 0.8-1.6"),
        ElementItem("Na", "钠", "138", "mmol/L", "正常 135-145"),
        ElementItem("Ca", "钙", "2.3", "mmol/L", "正常 2.1-2.6")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ClearDuDimens.ElementsGridGap)
    ) {
        elements.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.ElementsGridGap)
            ) {
                rowItems.forEach { item ->
                    ElementCard(
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

private data class ElementItem(
    val symbol: String,
    val name: String,
    val value: String,
    val unit: String,
    val range: String
)

@Composable
private fun ElementCard(item: ElementItem, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ClearDuDimens.ElementCardMinHeight),
        shape = RoundedCornerShape(ClearDuDimens.ElementCardRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = ClearDuDimens.ElementCardPaddingH,
                    vertical = ClearDuDimens.ElementCardPaddingV
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.symbol,
                style = ClearDuTypography.ElementSymbol,
                color = LiquidGlassColors.MedicalCyan,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.name,
                style = ClearDuTypography.ElementName,
                color = LiquidGlassColors.Text400,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.value,
                style = ClearDuTypography.ElementValue,
                color = LiquidGlassColors.Foreground,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.unit,
                style = ClearDuTypography.ElementUnit,
                color = LiquidGlassColors.Text400,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(ClearDuDimens.ElementRangeTopMargin))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(ClearDuDimens.ElementRangeRadius))
                    .drawBehindFill(LiquidGlassColors.TintGreenBg)
                    .padding(
                        horizontal = ClearDuDimens.ElementRangePaddingH,
                        vertical = ClearDuDimens.ElementRangePaddingV
                    )
            ) {
                Text(
                    text = item.range,
                    style = ClearDuTypography.ElementRange,
                    color = LiquidGlassColors.MedicalGreen,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun Modifier.drawBehindFill(color: Color): Modifier =
    this.then(
        Modifier.drawBehind {
            drawRect(color = color)
        }
    )
