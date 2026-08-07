package com.cleardu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * @param isBrightBackground 背景亮度自适应：true=亮色背景（加深玻璃底色+增强阴影），false=暗色背景（默认参数）
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
    isBrightBackground: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    // 背景亮度动态联动：亮色背景加深玻璃底色、增强阴影、边框调暗
    val animDuration = 600
    val animatedBg by animateColorAsState(
        targetValue = if (isBrightBackground) {
            // 亮色背景：加深玻璃底色，使用 rgba(0,0,0,0.25) 级别
            background.copy(
                red = (background.red * 0.3f).coerceIn(0f, 1f),
                green = (background.green * 0.3f).coerceIn(0f, 1f),
                blue = (background.blue * 0.3f).coerceIn(0f, 1f),
                alpha = (background.alpha * 1.8f).coerceIn(0f, 1f)
            )
        } else background,
        animationSpec = tween(durationMillis = animDuration),
        label = "glassBg"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isBrightBackground) {
            border.copy(alpha = (border.alpha * 0.6f).coerceIn(0f, 1f))
        } else border,
        animationSpec = tween(durationMillis = animDuration),
        label = "glassBorder"
    )
    val animatedShadowColor by animateColorAsState(
        targetValue = if (isBrightBackground) {
            if (shadowColor == Color.Transparent) LiquidGlassColors.BrightGlassBg
            else shadowColor.copy(alpha = (shadowColor.alpha * 1.5f).coerceIn(0f, 1f))
        } else shadowColor,
        animationSpec = tween(durationMillis = animDuration),
        label = "glassShadow"
    )
    val animatedShadowElevation = if (isBrightBackground && shadowElevation == 0f) 4f
        else if (isBrightBackground) shadowElevation * 1.3f
        else shadowElevation

    val animatedSpecular by animateColorAsState(
        targetValue = if (isBrightBackground) {
            specularTop.copy(alpha = (specularTop.alpha * 0.4f).coerceIn(0f, 1f))
        } else specularTop,
        animationSpec = tween(durationMillis = animDuration),
        label = "glassSpecular"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .let { mod ->
                if (animatedShadowElevation > 0f && animatedShadowColor != Color.Transparent) {
                    mod.shadow(animatedShadowElevation.dp, shape, clip = false, ambientColor = animatedShadowColor, spotColor = animatedShadowColor)
                } else mod
            }
            // Solid translucent fill — frosted look against dark mesh substrate.
            .drawBehindFill(animatedBg)
            // Specular overlay drawn on top of the content so the highlight
            // reads as light catching the glass surface.
            .drawSpecularOverlay(animatedSpecular)
            .border(BorderStroke(ClearDuDimens.GlassBorderWidth, animatedBorder), shape),
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