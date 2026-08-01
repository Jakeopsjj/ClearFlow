package com.cleardu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Mesh gradient background reproducing the reference HTML's `.mesh-bg`.
 *
 * The original uses five overlapping `radial-gradient` ellipses layered over a
 * solid `--background` substrate. We rebuild the same look with five
 * [Brush.radialGradient] passes drawn onto a Canvas — no WebView, no CSS.
 *
 * Anchor offsets are expressed as fractions of the canvas size so the mesh
 * scales cleanly to any device resolution.
 *
 * @param modifier outer layout modifier
 * @param background substrate color (defaults to dark-first #000000)
 */
@Composable
fun MeshGradientBackground(
    modifier: Modifier = Modifier,
    background: Color = LiquidGlassColors.Background,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .drawBehind {
                drawMeshGradient(size.width, size.height)
            }
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMeshGradient(
    width: Float,
    height: Float
) {
    // Reference (CSS, dark-first):
    //   radial-gradient(ellipse 320x300 at 10% 90%, mesh-purple)
    //   radial-gradient(ellipse 280x260 at 50%  5%, mesh-cyan)
    //   radial-gradient(ellipse 300x280 at 90% 85%, mesh-deep-purple)
    //   radial-gradient(ellipse 260x240 at 95% 10%, mesh-blue)
    //   radial-gradient(ellipse 200x200 at 20% 40%, mesh-cyan-extra)
    // Convert px → dp scales already handled by DrawScope density.

    val density = this.density

    fun ellipseGradient(
        centerX: Float,
        centerY: Float,
        radiusX: Float,
        radiusY: Float,
        color: Color
    ): Brush {
        val maxR = maxOf(radiusX, radiusY)
        return Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = Offset(centerX, centerY),
            radius = maxR,
            tileMode = TileMode.Clamp
        )
    }

    // 1. mesh-purple at 10% / 90%
    drawRect(
        brush = ellipseGradient(
            centerX = width * 0.10f,
            centerY = height * 0.90f,
            radiusX = ClearDuDimens.Mesh.PurpleRadiusX.value * density,
            radiusY = ClearDuDimens.Mesh.PurpleRadiusY.value * density,
            color = LiquidGlassColors.MeshPurple
        )
    )

    // 2. mesh-cyan at 50% / 5%
    drawRect(
        brush = ellipseGradient(
            centerX = width * 0.50f,
            centerY = height * 0.05f,
            radiusX = ClearDuDimens.Mesh.CyanRadiusX.value * density,
            radiusY = ClearDuDimens.Mesh.CyanRadiusY.value * density,
            color = LiquidGlassColors.MeshCyan
        )
    )

    // 3. mesh-deep-purple at 90% / 85%
    drawRect(
        brush = ellipseGradient(
            centerX = width * 0.90f,
            centerY = height * 0.85f,
            radiusX = ClearDuDimens.Mesh.DeepPurpleRadiusX.value * density,
            radiusY = ClearDuDimens.Mesh.DeepPurpleRadiusY.value * density,
            color = LiquidGlassColors.MeshDeepPurple
        )
    )

    // 4. mesh-blue at 95% / 10%
    drawRect(
        brush = ellipseGradient(
            centerX = width * 0.95f,
            centerY = height * 0.10f,
            radiusX = ClearDuDimens.Mesh.BlueRadiusX.value * density,
            radiusY = ClearDuDimens.Mesh.BlueRadiusY.value * density,
            color = LiquidGlassColors.MeshBlue
        )
    )

    // 5. mesh-cyan-extra at 20% / 40% (smaller, softer cyan wash)
    drawRect(
        brush = ellipseGradient(
            centerX = width * 0.20f,
            centerY = height * 0.40f,
            radiusX = ClearDuDimens.Mesh.GreenRadius.value * density,
            radiusY = ClearDuDimens.Mesh.GreenRadius.value * density,
            color = LiquidGlassColors.MeshAdditionalGreen.copy(alpha = 0.6f)
        )
    )
}
