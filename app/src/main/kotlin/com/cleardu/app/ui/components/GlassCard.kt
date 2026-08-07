package com.cleardu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * A liquid-glass translucent surface reproducing the reference `.glass` style:
 *   background   = rgba(255,255,255,0.12)
 *   backdrop     = blur(30px) saturate(1.8)        ← approximated via translucency
 *   border       = 1px rgba(255,255,255,0.22)
 *   box-shadow   = 0 8px 32px rgba(0,0,0,0.25), inset 0 1px 0 rgba(255,255,255,0.25)
 *
 * The specular highlight is drawn as a vertical gradient over the top half of
 * the card (mirrors `.glass::after`). All visuals are native Compose — no
 * WebView, no CSS backdrop-filter.
 *
 * @param shape corner shape (default 18dp matching `.permission-card`)
 * @param background translucent glass fill (overridable for tinted variants)
 * @param border glass border color
 * @param shadowColor optional shadow color for depth (default transparent = no shadow)
 * @param shadowElevation shadow elevation in dp (default 0 = no shadow)
 * @param specularTop specular highlight at the top of the card
 * @param content the card content
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(ClearDuDimens.PermissionCardRadius),
    background: Color = LiquidGlassColors.GlassBg,
    border: Color = LiquidGlassColors.GlassBorder,
    shadowColor: Color = Color.Transparent,
    shadowElevation: Float = 0f,
    specularTop: Color = LiquidGlassColors.GlassSpecularTop,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .let { mod ->
                if (shadowElevation > 0f && shadowColor != Color.Transparent) {
                    mod.shadow(shadowElevation.dp, shape, clip = false, ambientColor = shadowColor, spotColor = shadowColor)
                } else mod
            }
            // Solid translucent fill — frosted look against dark mesh substrate.
            .drawBehindFill(background)
            // Specular overlay drawn on top of the content so the highlight
            // reads as light catching the glass surface.
            .drawSpecularOverlay(specularTop)
            .border(BorderStroke(ClearDuDimens.GlassBorderWidth, border), shape),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Fills the composable with a translucent color, then draws content above it.
 */
private fun Modifier.drawBehindFill(color: Color): Modifier =
    this.drawWithContent {
        drawRect(color = color)
        drawContent()
    }

/**
 * Draws the `.glass::after` specular gradient over the top half:
 *   linear-gradient(180deg, glass-specular-top 0%, glass-specular-mid-low 50%, transparent 100%)
 */
private fun Modifier.drawSpecularOverlay(specularTop: Color): Modifier =
    this.drawWithContent {
        drawContent()
        val brush = Brush.verticalGradient(
            colors = listOf(
                specularTop.copy(alpha = 0.55f),
                specularTop.copy(alpha = 0.10f),
                Color.Transparent
            ),
            startY = 0f,
            endY = size.height * ClearDuDimens.GlassSpecularHeightFraction,
            tileMode = TileMode.Clamp
        )
        drawRect(brush = brush)
    }