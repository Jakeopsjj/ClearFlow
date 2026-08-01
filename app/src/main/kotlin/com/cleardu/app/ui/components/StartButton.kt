package com.cleardu.app.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.R
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuMotion
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * The primary "开始使用" call-to-action button.
 *
 * Reproduces `.start-btn` from the reference:
 *  - height 56dp, radius 28dp (pill)
 *  - linear-gradient(135deg, #5ac8fa → #007aff → #0064d6)
 *  - layered drop shadow: 0 8px 30px rgba(0,122,255,0.35) + 0 4px 12px rgba(90,200,250,0.2)
 *  - inset top specular highlight: linear-gradient(180deg, rgba(255,255,255,0.3) → 0.05)
 *  - press scale 0.96 + ripple feedback
 *  - `@keyframes btnGlow` 2-second ambient glow when [allGranted] is true
 *
 * @param allGranted enables the ambient glow animation when all permissions are granted
 * @param onClick invoked when the user taps the button
 */
@Composable
fun StartButton(
    allGranted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) ClearDuMotion.ButtonPressScale else 1f,
        label = "startButtonScale"
    )

    val transition = rememberInfiniteTransition(label = "startButtonGlow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.50f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ClearDuMotion.ButtonGlowDurationMs,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "startButtonGlowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = ClearDuDimens.StartButtonMaxWidth)
            .height(ClearDuDimens.StartButtonHeight)
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            .clip(RoundedCornerShape(ClearDuDimens.StartButtonRadius))
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(
                    bounded = true,
                    color = Color.White
                ),
                onClick = onClick
            )
            .drawWithContent {
                // Glow halo behind the button when [allGranted] is true
                if (allGranted) drawGlowHalo(glowAlpha)
                drawGradientBackground()
                drawContent()
                drawSpecularTop()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.action_start),
            style = ClearDuTypography.StartButton,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

private fun DrawScope.drawGradientBackground() {
    val brush = Brush.linearGradient(
        colors = listOf(
            LiquidGlassColors.StartGradientStart,
            LiquidGlassColors.StartGradientMid,
            LiquidGlassColors.StartGradientEnd
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(size.width, size.height)
    )
    drawRect(brush = brush)
}

/**
 * Soft outer glow halo when all permissions granted. Mirrors the second half of
 * `@keyframes btnGlow` (0 8px 40px rgba(0,122,255,0.5) + 0 4px 20px rgba(90,200,250,0.35)).
 */
private fun DrawScope.drawGlowHalo(alpha: Float) {
    val glowBrush = Brush.radialGradient(
        colors = listOf(
            LiquidGlassColors.MedicalBlue.copy(alpha = alpha),
            LiquidGlassColors.MedicalCyan.copy(alpha = alpha * 0.7f),
            Color.Transparent
        ),
        center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
        radius = maxOf(size.width, size.height) * 0.75f
    )
    drawRect(brush = glowBrush)
}

/**
 * Top specular highlight: linear-gradient(180deg, rgba(255,255,255,0.3) → 0.05),
 * covering the top 50% of the button as a "rounded-top, squared-bottom" pill cap.
 */
private fun DrawScope.drawSpecularTop() {
    val brush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.30f),
            Color.White.copy(alpha = 0.05f),
            Color.Transparent
        ),
        startY = 0f,
        endY = size.height * 0.5f
    )
    drawRect(brush = brush)
}
