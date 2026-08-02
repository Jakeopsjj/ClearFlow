package com.cleardu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.cleardu.app.R
import com.cleardu.app.data.MedicationReminder
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Medication reminder card.
 *
 * Reproduces `.med-reminder` from the reference:
 * - Glass card with icon + text + action button
 * - Orange-tinted icon background
 * - Press scale animation
 */
@Composable
fun MedicationReminderCard(
    reminder: MedicationReminder,
    onRemind: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (pressed) 0.98f else 1f
                scaleY = if (pressed) 0.98f else 1f
            }
            .clip(RoundedCornerShape(ClearDuDimens.MedReminderRadius))
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(
                    bounded = true,
                    color = Color.White
                ),
                onClick = onRemind
            ),
        shape = RoundedCornerShape(ClearDuDimens.MedReminderRadius),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorder,
        specularTop = LiquidGlassColors.GlassSpecularTop
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ClearDuDimens.MedReminderPaddingH,
                    vertical = ClearDuDimens.MedReminderPaddingV
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon + text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.MedInfoGap)
            ) {
                // Medication icon
                Box(
                    modifier = Modifier
                        .size(ClearDuDimens.MedIconSize)
                        .clip(RoundedCornerShape(ClearDuDimens.MedIconRadius))
                        .drawBehindFill(LiquidGlassColors.TintOrangeBg),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                        drawCircle(
                            color = LiquidGlassColors.MedicalOrange,
                            radius = 8.dp.toPx()
                        )
                    }
                }

                // Text
                Column {
                    Text(
                        text = reminder.title,
                        style = ClearDuTypography.MedTitle,
                        color = LiquidGlassColors.Foreground
                    )
                    Spacer(Modifier.height(ClearDuDimens.MedTitleDetailGap))
                    Text(
                        text = reminder.detail,
                        style = ClearDuTypography.MedDetail,
                        color = LiquidGlassColors.Text400
                    )
                }
            }

            // Remind button
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .drawBehindFill(LiquidGlassColors.TintCyanMd)
                    .border(
                        BorderStroke(1.dp, LiquidGlassColors.TintCyanBorder),
                        RoundedCornerShape(9999.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRemind
                    )
                    .padding(
                        horizontal = ClearDuDimens.RemindBtnPaddingH,
                        vertical = ClearDuDimens.RemindBtnPaddingV
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reminder.actionLabel,
                    style = ClearDuTypography.RemindButton,
                    color = LiquidGlassColors.MedicalCyan
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