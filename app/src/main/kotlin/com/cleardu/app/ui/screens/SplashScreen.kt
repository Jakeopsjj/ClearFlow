package com.cleardu.app.ui.screens

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.R
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors
import kotlinx.coroutines.delay

/**
 * Branded splash screen shown when the user re-opens the app (onboarding already complete).
 *
 * Inspired by WeChat's earth splash:
 *  - Dark gradient background with animated mesh glow
 *  - App logo (water drop) with pulsing animation
 *  - App name "清渡" and subtitle
 *  - Auto-navigates to the main activity after [SPLASH_DURATION_MS]
 *
 * @param onSplashFinished called after the splash animation duration
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = backgroundAwareColors()
    val transition = rememberInfiniteTransition(label = "splashGlow")

    // Pulsing glow for the background mesh
    val meshGlowAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "meshGlowAlpha"
    )

    // Pulsing glow for the logo ring
    val logoGlowAlpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoGlowAlpha"
    )

    // Auto-navigate after the splash duration
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        LiquidGlassColors.MeshCyan.copy(alpha = meshGlowAlpha),
                        LiquidGlassColors.MeshPurple.copy(alpha = meshGlowAlpha * 0.7f),
                        LiquidGlassColors.MeshDeepPurple.copy(alpha = meshGlowAlpha * 0.5f),
                        Color.Black
                    ),
                    center = Offset(0.3f * 1000f, 0.4f * 1000f), // scaled coordinates
                    radius = 1200f
                )
            )
            .background(Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0A1A),
                    Color(0xFF000000)
                )
            )),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // App logo — water drop in a glowing ring
            SplashLogo(
                size = ClearDuDimens.LogoSize,
                glowAlpha = logoGlowAlpha
            )

            Spacer(Modifier.height(24.dp))

            // App name
            androidx.compose.material3.Text(
                text = stringResource(R.string.splash_title),
                style = ClearDuTypography.WelcomeTitle.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit(
                        36f,
                        androidx.compose.ui.unit.TextUnitType.Sp
                    )
                ),
                color = colors.foreground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Subtitle
            androidx.compose.material3.Text(
                text = stringResource(R.string.splash_subtitle),
                style = ClearDuTypography.WelcomeSubtitle,
                color = colors.text400,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Splash logo — a simplified version of [AppLogo] with a pulsing outer glow.
 */
@Composable
private fun SplashLogo(
    size: androidx.compose.ui.unit.Dp,
    glowAlpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val center = Offset(canvasSize / 2f, canvasSize / 2f)

            // Outer pulsing glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LiquidGlassColors.MedicalCyan.copy(alpha = glowAlpha),
                        LiquidGlassColors.MedicalCyan.copy(alpha = glowAlpha * 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = canvasSize / 2f
                )
            )

            // Glass ring substrate
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        LiquidGlassColors.MedicalCyan.copy(alpha = 0.20f),
                        LiquidGlassColors.MedicalBlue.copy(alpha = 0.15f)
                    )
                ),
                radius = canvasSize / 2f - 4.dp.toPx(),
                center = center
            )

            // Inner specular top highlight
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    startY = center.y - (canvasSize / 2f),
                    endY = center.y
                ),
                radius = canvasSize / 2f - 4.dp.toPx(),
                center = center
            )

            // Border ring
            drawCircle(
                color = LiquidGlassColors.MedicalCyan.copy(alpha = 0.40f),
                radius = canvasSize / 2f - 4.dp.toPx(),
                center = center,
                style = Stroke(width = ClearDuDimens.LogoRingBorderWidth.toPx())
            )

            // Water drop
            drawWaterDrop(center, canvasSize)
        }
    }
}

/**
 * Draws the SVG water-drop path. Identical to [com.cleardu.app.ui.components.AppLogo]'s
 * water drop rendering.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWaterDrop(
    center: Offset,
    canvasSize: Float
) {
    val dropScale = canvasSize / 80f
    val dropWidth = 32f * dropScale
    val dropHeight = 40f * dropScale
    val dropLeft = center.x - dropWidth / 2f
    val dropTop = center.y - dropHeight / 2f

    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(16f * dropScale + dropLeft, 2f * dropScale + dropTop)
        cubicTo(
            16f * dropScale + dropLeft, 2f * dropScale + dropTop,
            4f * dropScale + dropLeft, 18f * dropScale + dropTop,
            4f * dropScale + dropLeft, 26f * dropScale + dropTop
        )
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                left = 4f * dropScale + dropLeft,
                top = 14f * dropScale + dropTop,
                right = 28f * dropScale + dropLeft,
                bottom = 38f * dropScale + dropTop
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        cubicTo(
            28f * dropScale + dropLeft, 18f * dropScale + dropTop,
            16f * dropScale + dropLeft, 2f * dropScale + dropTop,
            16f * dropScale + dropLeft, 2f * dropScale + dropTop
        )
        close()
    }

    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(
                LiquidGlassColors.MedicalCyan,
                LiquidGlassColors.MedicalBlue
            ),
            start = Offset(dropLeft, dropTop),
            end = Offset(dropLeft + dropWidth, dropTop + dropHeight)
        )
    )

    // Inner highlight
    drawOval(
        color = Color.White.copy(alpha = 0.30f),
        topLeft = Offset(
            (12f - 3f) * dropScale + dropLeft,
            (22f - 4f) * dropScale + dropTop
        ),
        size = androidx.compose.ui.geometry.Size(6f * dropScale, 8f * dropScale)
    )
}

/** Duration the splash screen is shown before navigating to the main activity. */
private const val SPLASH_DURATION_MS = 2000L