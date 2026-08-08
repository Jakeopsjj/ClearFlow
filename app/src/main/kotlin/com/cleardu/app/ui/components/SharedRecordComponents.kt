package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors

/**
 * Shared UI pieces used across every tab of the data-record page:
 * quick-note chips, a free-form note text area, and the gradient save button.
 *
 * All three are self-contained sections that include their own trailing bottom
 * margin (where the spec calls for one) so a record screen can simply stack
 * them in a [Column].
 */

/**
 * "快速备注" — a horizontally scrollable row of selectable time-context chips.
 * The selected chip fills with [LiquidGlassColors.TintCyanMd], tints its text
 * [LiquidGlassColors.MedicalCyan], and gains a [LiquidGlassColors.TintCyanGlow]
 * border. Tapping a chip scales it to 0.95 and selects it.
 *
 * @param selectedIndex currently selected chip index
 * @param onSelected callback when a chip is selected
 * @param modifier outer modifier
 */
@Composable
fun QuickNoteChips(
    selectedIndex: Int = 0,
    onSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = backgroundAwareColors()
    val chips = listOf("透析前", "透析后", "晨起", "睡前", "运动后")

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "快速备注",
            style = ClearDuTypography.MedListTitle,
            color = colors.text400,
            modifier = Modifier.padding(start = 2.dp)
        )
        Spacer(Modifier.height(ClearDuDimens.ChipsLabelBottomMargin))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.ChipGap)
        ) {
            chips.forEachIndexed { index, label ->
                QuickNoteChip(
                    label = label,
                    isSelected = index == selectedIndex,
                    onClick = { onSelected(index) }
                )
            }
        }
        Spacer(Modifier.height(ClearDuDimens.ChipsSectionBottomMargin))
    }
}

@Composable
private fun QuickNoteChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = backgroundAwareColors()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        label = "quickNoteChipScale"
    )

    GlassCard(
        modifier = modifier
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
        background = if (isSelected) LiquidGlassColors.TintCyanMd else LiquidGlassColors.GlassBg,
        border = if (isSelected) LiquidGlassColors.TintCyanGlow else LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Text(
            text = label,
            style = ClearDuTypography.ChipText,
            color = if (isSelected) LiquidGlassColors.MedicalCyan else colors.text300,
            modifier = Modifier.padding(
                horizontal = ClearDuDimens.ChipPaddingH,
                vertical = ClearDuDimens.ChipPaddingV
            )
        )
    }
}

/**
 * "添加备注..." — a translucent glass text area with a 72dp minimum height,
 * 1.5x line height, and a cyan cursor. The placeholder uses
 * [LiquidGlassColors.PlaceholderInputStrong] until the user types.
 *
 * @param noteText current note text
 * @param onNoteChange callback when note text changes
 * @param modifier outer modifier
 */
@Composable
fun NoteTextArea(
    noteText: String = "",
    onNoteChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = backgroundAwareColors()

    Column(modifier = modifier.fillMaxWidth()) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ClearDuDimens.NoteAreaMinHeight),
            shape = RoundedCornerShape(ClearDuDimens.NoteAreaRadius),
            background = LiquidGlassColors.GlassBgLight,
            border = LiquidGlassColors.GlassBorderSubtle,
            specularTop = LiquidGlassColors.GlassSpecularTop
        ) {
            BasicTextField(
                value = noteText,
                onValueChange = onNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ClearDuDimens.NoteAreaPaddingH,
                        vertical = ClearDuDimens.NoteAreaPaddingV
                    ),
                textStyle = ClearDuTypography.NoteText.copy(
                    color = colors.foreground
                ),
                cursorBrush = SolidColor(LiquidGlassColors.MedicalCyan),
                decorationBox = { inner ->
                    Box {
                        if (noteText.isEmpty()) {
                            Text(
                                text = "添加备注...",
                                style = ClearDuTypography.NoteText.copy(
                                    color = LiquidGlassColors.PlaceholderInputStrong
                                )
                            )
                        }
                        inner()
                    }
                }
            )
        }
        Spacer(Modifier.height(ClearDuDimens.NoteAreaBottomMargin))
    }
}

/**
 * "保存记录" — full-width pill call-to-action. The face is a 135°
 * MedicalCyan → MedicalBlue gradient with a top-half specular highlight; a
 * layered colored glow (ambient cyan + offset blue drop shadow) renders behind
 * it. Pressing scales to 0.97 and dims the glow.
 *
 * @param onClick invoked when the button is tapped
 */
@Composable
fun SaveRecordButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        label = "saveRecordButtonScale"
    )
    val glowPadding = 8.dp
    val cyanGlow = if (pressed) LiquidGlassColors.BtnGlowCyanActive else LiquidGlassColors.BtnGlowCyan
    val blueGlow = if (pressed) LiquidGlassColors.BtnGlowBlueActive else LiquidGlassColors.BtnGlowBlue

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ClearDuDimens.SaveBtnHeight + glowPadding * 2)
            .clip(RoundedCornerShape(ClearDuDimens.SaveBtnRadius))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                // Ambient cyan glow halo — draw as a circle, not a rect,
                // so the glow is soft and circular rather than a hard rectangle.
                drawCircle(
                    Brush.radialGradient(
                        colors = listOf(
                            cyanGlow,
                            cyanGlow.copy(alpha = cyanGlow.alpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = maxOf(size.width, size.height) * 0.7f
                    ),
                    radius = maxOf(size.width, size.height) * 0.7f,
                    center = Offset(centerX, centerY)
                )
                // Offset blue drop shadow.
                drawCircle(
                    Brush.radialGradient(
                        colors = listOf(blueGlow, Color.Transparent),
                        center = Offset(centerX, centerY + 8.dp.toPx()),
                        radius = maxOf(size.width, size.height) * 0.6f
                    ),
                    radius = maxOf(size.width, size.height) * 0.6f,
                    center = Offset(centerX, centerY + 8.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ClearDuDimens.SaveBtnHeight)
                .clip(RoundedCornerShape(ClearDuDimens.SaveBtnRadius))
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onClick
                )
                .drawWithContent {
                    // 135° gradient face: MedicalCyan → MedicalBlue.
                    drawRect(
                        Brush.linearGradient(
                            colors = listOf(
                                LiquidGlassColors.MedicalCyan,
                                LiquidGlassColors.MedicalBlue
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                    drawContent()
                    // Specular highlight over the top half.
                    drawRect(
                        Brush.verticalGradient(
                            colors = listOf(
                                LiquidGlassColors.GlassSpecularTop.copy(alpha = 0.55f),
                                LiquidGlassColors.GlassSpecularTop.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height * 0.5f
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "保存记录",
                style = ClearDuTypography.SaveBtnText,
                color = LiquidGlassColors.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
