package com.cleardu.app.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.data.QuickAction
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Quick action buttons row.
 *
 * Reproduces `.quick-actions` from the reference:
 * - 4 circular glass buttons with icons
 * - Labels below each button
 * - Press scale animation
 */
@Composable
fun QuickActionsRow(
    actions: List<QuickAction>,
    onAction: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ClearDuDimens.QuickActionsPaddingH),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach { action ->
            QuickActionButton(
                action = action,
                onClick = { onAction(action) }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    action: QuickAction,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ClearDuDimens.QuickActionGap)
    ) {
        GlassCard(
            modifier = Modifier
                .size(ClearDuDimens.QuickActionBtnSize)
                .graphicsLayer {
                    scaleX = if (pressed) 0.92f else 1f
                    scaleY = if (pressed) 0.92f else 1f
                }
                .clip(CircleShape)
                .clickable(
                    interactionSource = interaction,
                    indication = androidx.compose.material3.ripple(
                        bounded = true,
                        color = Color.White
                    ),
                    onClick = onClick
                ),
            shape = CircleShape,
            background = LiquidGlassColors.GlassBg,
            border = LiquidGlassColors.GlassBorder,
            specularTop = LiquidGlassColors.GlassSpecularTop
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Accent dot as icon placeholder
                androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
                    drawCircle(
                        color = action.accentColor,
                        radius = 10.dp.toPx()
                    )
                }
            }
        }

        Text(
            text = action.label,
            style = ClearDuTypography.QuickActionLabel,
            color = LiquidGlassColors.Text400,
            textAlign = TextAlign.Center
        )
    }
}
