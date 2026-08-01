package com.cleardu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cleardu.app.data.PermissionItem
import com.cleardu.app.data.PermissionTone
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuMotion
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * A single permission card on the onboarding screen.
 *
 * Reproduces `.permission-card.glass.card-interactive` with:
 *  - translucent glass substrate (overridden with a red→orange tint when
 *    [PermissionItem.critical] and not yet granted)
 *  - tinted circular icon (`.perm-icon-{tone}`)
 *  - permission name + description
 *  - "允许" pill button that toggles to a green "已允许" state on grant
 *  - critical badge "关键权限" overlay (top-right) for critical cards
 *  - press-to-scale + ripple feedback (CSS `:active { transform: scale(0.98) }`)
 *
 * The whole card surface is clickable so any tap triggers the grant (matching
 * the original HTML behavior). Actual Android runtime permission requests are
 * issued by the host Activity when [onAllow] fires — this Composable is purely
 * presentational and stays testable in isolation.
 *
 * @param item permission descriptor
 * @param granted whether the permission has been granted (drives button state)
 * @param onAllow invoked when the user taps the card / button to allow
 */
@Composable
fun PermissionCard(
    item: PermissionItem,
    granted: Boolean,
    onAllow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardInteraction = remember { MutableInteractionSource() }
    val cardPressed by cardInteraction.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (cardPressed) ClearDuMotion.CardPressScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardPressScale"
    )

    val cardShape: Shape = RoundedCornerShape(ClearDuDimens.PermissionCardRadius)

    val backgroundBrush: Brush = when {
        granted && item.critical -> Brush.linearGradient(
            colors = listOf(
                LiquidGlassColors.TintGreenBg,
                LiquidGlassColors.GlassBgLight
            )
        )
        granted -> Brush.linearGradient(
            colors = listOf(
                LiquidGlassColors.GlassBgLight,
                LiquidGlassColors.GlassBgLight
            )
        )
        item.critical -> Brush.linearGradient(
            colors = listOf(
                LiquidGlassColors.CriticalRedTint,
                LiquidGlassColors.CriticalOrangeTint
            )
        )
        else -> Brush.linearGradient(
            colors = listOf(
                LiquidGlassColors.GlassBg,
                LiquidGlassColors.GlassBg
            )
        )
    }

    val borderColor by animateColorAsState(
        targetValue = when {
            granted && item.critical -> LiquidGlassColors.TintGreenBorder.copy(alpha = 0.20f)
            granted -> LiquidGlassColors.GlassBorderSubtle
            item.critical -> LiquidGlassColors.TintRedBorder.copy(alpha = 0.25f)
            else -> LiquidGlassColors.GlassBorder
        },
        label = "cardBorderColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            .clip(cardShape)
            .clickable(
                interactionSource = cardInteraction,
                indication = ripple(bounded = true, color = LiquidGlassColors.MedicalCyan)
            ) { onAllow() }
            .drawWithContent {
                drawRect(brush = backgroundBrush)
                drawContent()
                drawSpecularOverlay()
            }
            .border(BorderStroke(ClearDuDimens.GlassBorderWidth, borderColor), cardShape)
            .padding(
                horizontal = ClearDuDimens.PermissionCardPaddingHorizontal,
                vertical = ClearDuDimens.PermissionCardPaddingVertical
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PermissionIcon(item.icon, item.tone)
            Spacer(Modifier.width(ClearDuDimens.PermissionCardGap))
            PermissionText(item)
            Spacer(Modifier.width(ClearDuDimens.PermissionCardGap))
            PermissionToggleButton(
                granted = granted,
                critical = item.critical,
                onClick = onAllow
            )
        }

        if (item.critical && !granted) {
            CriticalBadge(
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun PermissionIcon(icon: ImageVector, tone: PermissionTone) {
    Box(
        modifier = Modifier
            .size(ClearDuDimens.PermIconSize)
            .clip(CircleShape)
            .background(tone.tintBackground)
            .border(
                BorderStroke(ClearDuDimens.GlassBorderWidth, tone.tintBorder),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tone.iconColor,
            modifier = Modifier.size(ClearDuDimens.PermIconStrokeWidth)
        )
    }
}

@Composable
private fun PermissionText(item: PermissionItem) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = stringResource(item.nameRes),
            style = ClearDuTypography.PermissionName,
            color = if (item.critical) LiquidGlassColors.MedicalRed else LiquidGlassColors.Foreground
        )
        Spacer(Modifier.size(2.dp))
        Text(
            text = stringResource(item.descRes),
            style = ClearDuTypography.PermissionDesc,
            color = LiquidGlassColors.Text400
        )
    }
}

@Composable
private fun PermissionToggleButton(
    granted: Boolean,
    critical: Boolean,
    onClick: () -> Unit
) {
    val buttonBg by animateColorAsState(
        targetValue = when {
            granted -> LiquidGlassColors.TintGreenStrong
            critical -> LiquidGlassColors.MedicalOrange.copy(alpha = 0.18f)
            else -> LiquidGlassColors.GlassBgLight
        },
        label = "buttonBg"
    )
    val buttonFg by animateColorAsState(
        targetValue = when {
            granted -> LiquidGlassColors.MedicalGreen
            critical -> LiquidGlassColors.MedicalOrange
            else -> LiquidGlassColors.Text300
        },
        label = "buttonFg"
    )
    val buttonBorder by animateColorAsState(
        targetValue = when {
            granted -> LiquidGlassColors.MedicalGreen
            critical -> LiquidGlassColors.TintRedBorder.copy(alpha = 0.30f)
            else -> LiquidGlassColors.GlassBorder
        },
        label = "buttonBorder"
    )

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) ClearDuMotion.PermissionButtonPressScale else 1f,
        label = "buttonScale"
    )

    val label = if (granted) {
        stringResource(com.cleardu.app.R.string.perm_action_granted)
    } else {
        stringResource(com.cleardu.app.R.string.perm_action_allow)
    }

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(buttonBg)
            .border(BorderStroke(ClearDuDimens.GlassBorderWidth, buttonBorder), CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true, color = buttonFg),
                onClick = onClick
            )
            .padding(
                horizontal = if (granted)
                    ClearDuDimens.PermButtonPaddingHorizontalGranted
                else ClearDuDimens.PermButtonPaddingHorizontal,
                vertical = ClearDuDimens.PermButtonPaddingVertical
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (granted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = buttonFg,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(ClearDuDimens.PermButtonGap))
            }
            Text(
                text = label,
                style = ClearDuTypography.PermissionButton,
                color = buttonFg
            )
        }
    }
}

@Composable
private fun CriticalBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(
                top = ClearDuDimens.CriticalBadgeOffsetTop,
                end = ClearDuDimens.CriticalBadgeOffsetEnd
            )
            .clip(RoundedCornerShape(ClearDuDimens.CriticalBadgeRadius))
            .background(LiquidGlassColors.CriticalBadgeBg)
            .padding(
                horizontal = ClearDuDimens.CriticalBadgePaddingHorizontal,
                vertical = ClearDuDimens.CriticalBadgePaddingVertical
            )
    ) {
        Text(
            text = stringResource(com.cleardu.app.R.string.perm_critical_badge),
            style = ClearDuTypography.CriticalBadge,
            color = LiquidGlassColors.MedicalRed
        )
    }
}

/**
 * Specular highlight overlay (top-half gradient) drawn over the card content.
 * Mirrors `.glass::after`:
 *   linear-gradient(180deg, glass-specular-top 0%, glass-specular-mid-low 50%, transparent 100%)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSpecularOverlay() {
    val brush = Brush.verticalGradient(
        colors = listOf(
            LiquidGlassColors.GlassSpecularTop.copy(alpha = 0.55f),
            LiquidGlassColors.GlassSpecularMidLow.copy(alpha = 0.10f),
            Color.Transparent
        ),
        startY = 0f,
        endY = size.height * ClearDuDimens.GlassSpecularHeightFraction,
        tileMode = TileMode.Clamp
    )
    drawRect(brush = brush)
}
