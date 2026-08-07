package com.cleardu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors

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
 * ## 背景亮度感知（需求2）
 *
 * 当参数未显式指定（Color.Unspecified / shadowElevation = -1f）时，GlassCard
 * 自动根据天气背景亮度选择颜色：
 * - 背景偏亮：使用深色磨砂玻璃（BrightGlass*），加深底色、增强阴影、添加模糊，
 *   文字图标使用黑色。
 * - 背景偏暗：维持原始默认参数（Glass*），文字图标使用白色。
 *
 * 所有颜色和模糊参数通过 animateColorAsState / animateDpAsState 平滑过渡，
 * 确保在多波纹背景切换动画过程中无突兀跳变。
 *
 * @param shape corner shape (default 18dp matching `.permission-card`)
 * @param background translucent glass fill (Color.Unspecified = auto-detect from brightness)
 * @param border glass border color (Color.Unspecified = auto-detect)
 * @param shadowColor shadow color (Color.Unspecified = auto-detect)
 * @param shadowElevation shadow elevation in dp (-1f = auto-detect; 0f = no shadow)
 * @param specularTop specular highlight at the top of the card (Color.Unspecified = auto-detect)
 * @param blurRadius blur radius for frosted effect (0.dp = auto-detect; >0 = explicit)
 * @param content the card content
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(ClearDuDimens.PermissionCardRadius),
    background: Color = Color.Unspecified,
    border: Color = Color.Unspecified,
    shadowColor: Color = Color.Unspecified,
    shadowElevation: Float = -1f,
    specularTop: Color = Color.Unspecified,
    blurRadius: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColors = backgroundAwareColors()

    // Resolve unspecified colors based on background brightness
    val resolvedBackground = if (background != Color.Unspecified) background else bgColors.glassBg
    val resolvedBorder = if (border != Color.Unspecified) border else bgColors.glassBorder
    val resolvedShadowColor = if (shadowColor != Color.Unspecified) shadowColor else bgColors.glassShadow
    val resolvedShadowElevation = if (shadowElevation >= 0f) shadowElevation else if (bgColors.isBright) 6f else 0f
    val resolvedSpecularTop = if (specularTop != Color.Unspecified) specularTop else bgColors.glassSpecularTop
    val resolvedBlurRadius = if (blurRadius > 0.dp) blurRadius else if (bgColors.isBright) 8.dp else 0.dp

    // Animate transitions for smooth parameter changes during background switch
    val animColorSpec = tween<Color>(durationMillis = 600)
    val animDpSpec = tween<Dp>(durationMillis = 600)

    val animatedBackground by animateColorAsState(resolvedBackground, animColorSpec, label = "glassBg")
    val animatedBorder by animateColorAsState(resolvedBorder, animColorSpec, label = "glassBorder")
    val animatedShadowColor by animateColorAsState(resolvedShadowColor, animColorSpec, label = "glassShadowColor")
    val animatedSpecularTop by animateColorAsState(resolvedSpecularTop, animColorSpec, label = "glassSpecular")
    val animatedBlurRadius by animateDpAsState(resolvedBlurRadius, animDpSpec, label = "glassBlur")

    Box(
        modifier = modifier
            .clip(shape)
            .let { mod ->
                if (resolvedShadowElevation > 0f && animatedShadowColor != Color.Transparent) {
                    mod.shadow(
                        resolvedShadowElevation.dp, shape, clip = false,
                        ambientColor = animatedShadowColor, spotColor = animatedShadowColor
                    )
                } else mod
            },
        contentAlignment = Alignment.Center
    ) {
        // Glass fill layer — blurred when bright for enhanced frosted effect.
        // Blur is isolated to this layer so content/specular/border stay sharp.
        if (animatedBlurRadius > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(animatedBlurRadius)
                    .drawBehindFill(animatedBackground)
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehindFill(animatedBackground)
            )
        }

        // Content layer — specular overlay + border + actual content (all sharp)
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawSpecularOverlay(animatedSpecularTop)
                .border(BorderStroke(ClearDuDimens.GlassBorderWidth, animatedBorder), shape),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
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
