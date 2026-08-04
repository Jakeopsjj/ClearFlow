package com.cleardu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.cleardu.app.data.MedicationDose
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * "用药" tab content for the data-record page.
 *
 * A glass search bar, a "已选药品" section header, and a vertical list of
 * medication items. Each item pairs a tinted capsule icon + name/dose with a
 * row of dose-multiplier buttons; the active button is filled with
 * [LiquidGlassColors.TintCyanMd] and tinted [LiquidGlassColors.MedicalCyan].
 *
 * @param medications list of currently selected medications
 * @param onMedicationsChange callback when the medication list changes
 * @param modifier outer modifier
 */
@Composable
fun MedicationPanel(
    medications: List<MedicationDose> = listOf(
        MedicationDose(name = "降压药", detail = "缬沙坦 80mg", doseMultiplier = 1.0),
        MedicationDose(name = "磷结合剂", detail = "碳酸钙 500mg", doseMultiplier = 1.0),
        MedicationDose(name = "促红细胞生成素", detail = "3000 IU", doseMultiplier = 1.0)
    ),
    onMedicationsChange: (List<MedicationDose>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MedicationSearchBar()
        Spacer(Modifier.height(ClearDuDimens.MedSearchBottomMargin))

        Text(
            text = "已选药品",
            style = ClearDuTypography.MedListTitle,
            color = LiquidGlassColors.Text400,
            modifier = Modifier.padding(start = 2.dp)
        )
        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(ClearDuDimens.MedItemGap)) {
            medications.forEachIndexed { index, med ->
                val iconColors = medicationIconColors(index)
                MedicationItem(
                    name = med.name,
                    dose = med.detail,
                    doseOptions = listOf("0.5", "1", "2"),
                    initialSelectedIndex = when {
                        med.doseMultiplier == 0.5 -> 0
                        med.doseMultiplier == 2.0 -> 2
                        else -> 1
                    },
                    iconBg = iconColors.first,
                    iconTint = iconColors.second,
                    onDoseChanged = { newMultiplier ->
                        val updated = medications.toMutableList().apply {
                            set(index, med.copy(doseMultiplier = newMultiplier))
                        }
                        onMedicationsChange(updated)
                    }
                )
            }
        }
    }
}

@Composable
private fun MedicationSearchBar(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.MedSearchRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ClearDuDimens.MedSearchPaddingH,
                    vertical = ClearDuDimens.MedSearchPaddingV
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = LiquidGlassColors.Text400,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(ClearDuDimens.MedSearchGap))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                textStyle = ClearDuTypography.MedSearchPlaceholder.copy(
                    color = LiquidGlassColors.Foreground
                ),
                singleLine = true,
                cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "搜索药品名称...",
                                style = ClearDuTypography.MedSearchPlaceholder,
                                color = LiquidGlassColors.PlaceholderInput
                            )
                        }
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
private fun MedicationItem(
    name: String,
    dose: String,
    doseOptions: List<String>,
    initialSelectedIndex: Int,
    iconBg: Color,
    iconTint: Color,
    onDoseChanged: (Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(initialSelectedIndex) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.MedItemRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ClearDuDimens.MedItemPaddingH,
                    vertical = ClearDuDimens.MedItemPaddingV
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.MedItemInfoGap)
            ) {
                Box(
                    modifier = Modifier
                        .size(ClearDuDimens.MedItemIconSize)
                        .clip(RoundedCornerShape(ClearDuDimens.MedItemIconRadius))
                        .drawBehindFill(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    CapsuleIcon(tintColor = iconTint)
                }
                Column {
                    Text(
                        text = name,
                        style = ClearDuTypography.MedItemName,
                        color = LiquidGlassColors.Foreground
                    )
                    Text(
                        text = dose,
                        style = ClearDuTypography.MedDetail,
                        color = LiquidGlassColors.Text400
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.MedDoseBtnGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                doseOptions.forEachIndexed { index, label ->
                    DoseButton(
                        label = label,
                        isActive = index == selectedIndex,
                        onClick = {
                            selectedIndex = index
                            onDoseChanged(label.toDouble())
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DoseButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }

    GlassCard(
        modifier = modifier
            .clip(RoundedCornerShape(ClearDuDimens.MedDoseBtnRadius))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(ClearDuDimens.MedDoseBtnRadius),
        background = if (isActive) LiquidGlassColors.TintCyanMd else LiquidGlassColors.GlassBg,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Text(
            text = label,
            style = ClearDuTypography.MedDoseBtn,
            color = if (isActive) LiquidGlassColors.MedicalCyan else LiquidGlassColors.Text400,
            modifier = Modifier.padding(
                horizontal = ClearDuDimens.MedDoseBtnPaddingH,
                vertical = ClearDuDimens.MedDoseBtnPaddingV
            )
        )
    }
}

/**
 * A 16dp capsule (pill) icon: a rounded rectangle rotated 45° with a
 * perpendicular split line through its middle, stroked with [tintColor].
 */
@Composable
private fun CapsuleIcon(
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(16.dp)) {
        val s = size.width / 16f
        val stroke = 1.4f * s
        val capsuleW = 6f * s
        val capsuleH = 12f * s
        val capsuleLeft = (size.width - capsuleW) / 2f
        val capsuleTop = (size.height - capsuleH) / 2f
        val midY = capsuleTop + capsuleH / 2f

        rotate(degrees = 45f, pivot = center) {
            drawRoundRect(
                color = tintColor,
                topLeft = Offset(capsuleLeft, capsuleTop),
                size = Size(capsuleW, capsuleH),
                cornerRadius = CornerRadius(capsuleW / 2f, capsuleW / 2f),
                style = Stroke(stroke)
            )
            drawLine(
                color = tintColor,
                start = Offset(capsuleLeft, midY),
                end = Offset(capsuleLeft + capsuleW, midY),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun Modifier.drawBehindFill(color: Color): Modifier =
    this.then(
        Modifier.drawBehind {
            drawRect(color = color)
        }
    )

/**
 * Returns a (background, tint) color pair for medication items,
 * cycling through preset colors based on index.
 */
private fun medicationIconColors(index: Int): Pair<Color, Color> {
    val pairs = listOf(
        LiquidGlassColors.TintPurpleBg to LiquidGlassColors.MedicalPurple,
        LiquidGlassColors.TintOrangeBg to LiquidGlassColors.MedicalOrange,
        LiquidGlassColors.TintGreenBg to LiquidGlassColors.MedicalGreen,
        LiquidGlassColors.TintRedBg to LiquidGlassColors.MedicalRed,
        LiquidGlassColors.TintCyanMd to LiquidGlassColors.MedicalCyan
    )
    return pairs[index % pairs.size]
}
