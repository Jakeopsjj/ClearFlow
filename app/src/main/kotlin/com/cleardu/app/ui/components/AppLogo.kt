package com.cleardu.app.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuMotion
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * App logo: a water drop inside a glowing glass ring.
 *
 * Reproduces the reference `.app-logo / .logo-ring / .logo-drop` block, including
 * the `@keyframes logoPulse` 3-second ambient glow. Animations are native Compose
 * `infiniteTransition` + `animateFloat` — no CSS, no JS.
 *
 * @param size overall diameter of the ring
 * @param modifier outer layout modifier
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = ClearDuDimens.LogoSize
) {
    val transition = rememberInfiniteTransition(label = "logoPulse")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = ClearDuMotion.LogoPulseDurationMs,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoGlow"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val center = Offset(canvasSize / 2f, canvasSize / 2f)

            // Outer pulsing glow halo (matches `0 0 30px rgba(90,200,250,0.2/0.35)`)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LiquidGlassColors.MedicalCyan.copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = canvasSize / 2f
                )
            )

            // Glass ring substrate: linear-gradient(135deg, rgba(90,200,250,0.2), rgba(0,122,255,0.15))
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

            // Inner specular top highlight: linear-gradient(180deg, rgba(255,255,255,0.2), transparent 50%)
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

            // Border ring: 2px solid rgba(90, 200, 250, 0.4)
            drawCircle(
                color = LiquidGlassColors.MedicalCyan.copy(alpha = 0.40f),
                radius = canvasSize / 2f - 4.dp.toPx(),
                center = center,
                style = Stroke(width = ClearDuDimens.LogoRingBorderWidth.toPx())
            )

            // Water drop — gradient-filled teardrop path
            drawWaterDrop(center, canvasSize)
        }
    }
}

/**
 * Draws the SVG water-drop path from the reference HTML:
 *   <path d="M16 2C16 2 4 18 4 26a12 12 0 0024 0C28 18 16 2 16 2z"/>
 *
 * Path is normalized to a 32x40 viewBox and centered inside [canvasSize].
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWaterDrop(
    center: Offset,
    canvasSize: Float
) {
    // Reference: .logo-drop is 32x40 px inside an 80x80 .logo-ring.
    // So 1 SVG unit = canvasSize / 80 px.
    val dropScale = canvasSize / 80f
    val dropWidth = 32f * dropScale
    val dropHeight = 40f * dropScale
    val dropLeft = center.x - dropWidth / 2f
    val dropTop = center.y - dropHeight / 2f

    // Drop body path
    val path = androidx.compose.ui.graphics.Path().apply {
        // SVG: M16 2C16 2 4 18 4 26a12 12 0 0024 0C28 18 16 2 16 2z
        moveTo(16f * dropScale + dropLeft, 2f * dropScale + dropTop)
        cubicTo(
            16f * dropScale + dropLeft, 2f * dropScale + dropTop,
            4f * dropScale + dropLeft, 18f * dropScale + dropTop,
            4f * dropScale + dropLeft, 26f * dropScale + dropTop
        )
        // a12 12 0 0024 0 → arc from (4,26) to (28,26) sweeping via the BOTTOM.
        // In Compose, clockwise = positive sweep (visually, in y-down). SVG
        // sweep-flag=0 maps to counterclockwise (negative sweep) so the arc
        // bulges downward — the rounded bottom of the water drop.
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

    // Fill with the drop gradient: linear-gradient(0%→100%, #5ac8fa → #007aff)
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

    // Inner highlight: ellipse cx=12 cy=22 rx=3 ry=4 with rgba(255,255,255,0.3)
    drawOval(
        color = Color.White.copy(alpha = 0.30f),
        topLeft = Offset(
            (12f - 3f) * dropScale + dropLeft,
            (22f - 4f) * dropScale + dropTop
        ),
        size = androidx.compose.ui.geometry.Size(6f * dropScale, 8f * dropScale)
    )
}
