package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors
import java.util.Locale

/**
 * "元素检测" tab content for the data-record page.
 *
 * A 2x2 grid of glass cards for the four dialysis-critical electrolytes
 * (K / P / Na / Ca). Each card shows the element symbol, Chinese name,
 * current value, unit, a green "正常 x-y" range badge, and +/- adjust buttons.
 *
 * @param potassium current potassium value in mmol/L
 * @param phosphorus current phosphorus value in mmol/L
 * @param sodium current sodium value in mmol/L
 * @param calcium current calcium value in mmol/L
 * @param onPotassiumChange callback when potassium changes
 * @param onPhosphorusChange callback when phosphorus changes
 * @param onSodiumChange callback when sodium changes
 * @param onCalciumChange callback when calcium changes
 * @param modifier outer modifier
 */
@Composable
fun ElementsPanel(
    potassium: Double = 4.2,
    phosphorus: Double = 1.5,
    sodium: Double = 138.0,
    calcium: Double = 2.3,
    onPotassiumChange: (Double) -> Unit = {},
    onPhosphorusChange: (Double) -> Unit = {},
    onSodiumChange: (Double) -> Unit = {},
    onCalciumChange: (Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val elements = listOf(
        EditableElementItem("K", "钾", potassium, "mmol/L", "正常 3.5-5.5", 0.1, onPotassiumChange),
        EditableElementItem("P", "磷", phosphorus, "mmol/L", "正常 0.8-1.6", 0.1, onPhosphorusChange),
        EditableElementItem("Na", "钠", sodium, "mmol/L", "正常 135-145", 1.0, onSodiumChange),
        EditableElementItem("Ca", "钙", calcium, "mmol/L", "正常 2.1-2.6", 0.1, onCalciumChange)
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
                    EditableElementCard(
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

private data class EditableElementItem(
    val symbol: String,
    val name: String,
    val value: Double,
    val unit: String,
    val range: String,
    val step: Double,
    val onChange: (Double) -> Unit
)

@Composable
private fun EditableElementCard(item: EditableElementItem, modifier: Modifier = Modifier) {
    val colors = backgroundAwareColors()
    val formattedValue = if (item.value == item.value.toLong().toDouble() && item.value < 1000) {
        String.format(Locale.US, "%.0f", item.value)
    } else {
        String.format(Locale.US, "%.1f", item.value)
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ClearDuDimens.ElementCardMinHeight),
        shape = RoundedCornerShape(ClearDuDimens.ElementCardRadius),
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
                color = colors.text400,
                textAlign = TextAlign.Center
            )
            Text(
                text = formattedValue,
                style = ClearDuTypography.ElementValue,
                color = colors.foreground,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.unit,
                style = ClearDuTypography.ElementUnit,
                color = colors.text400,
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
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElementAdjustButton(
                    text = "-",
                    onClick = { item.onChange(roundToStep(item.value - item.step, item.step)) }
                )
                ElementAdjustButton(
                    text = "+",
                    onClick = { item.onChange(roundToStep(item.value + item.step, item.step)) }
                )
            }
        }
    }
}

@Composable
private fun ElementAdjustButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        label = "elementAdjustScale"
    )

    GlassCard(
        modifier = modifier
            .size(28.dp)
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

private fun roundToStep(value: Double, step: Double): Double {
    val factor = 1.0 / step
    return Math.round(value * factor) / factor
}

private fun Modifier.drawBehindFill(color: Color): Modifier =
    this.then(
        Modifier.drawBehind {
            drawRect(color = color)
        }
    )