package com.cleardu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.glassParams

/**
 * A liquid-glass translucent surface reproducing the reference `.glass` style.
 *
 * 当未显式传入颜色参数时，自动根据天气背景亮度自适应：
 * - 亮背景 → 浅灰磨砂卡片
 * - 暗背景 → 浅白磨砂卡片
 *
 * @param shape corner shape (default 18dp matching `.permission-card`)
 * @param background translucent glass fill (null = 自适应玻璃色)
 * @param border glass border color (null = 自适应边框色)
 * @param shadowColor optional shadow color for depth
 * @param shadowElevation shadow elevation in dp
 * @param specularTop specular highlight at the top of the card (null = 自适应高光)
 * @param content the card content
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(ClearDuDimens.PermissionCardRadius),
    background: Color? = null,
    border: Color? = null,
    shadowColor: Color? = null,
    shadowElevation: Float? = null,
    specularTop: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val params = glassParams()
    val bg = background ?: params.background
    val bd = border ?: params.border
    val sc = shadowColor ?: params.shadowColor
    val se = shadowElevation ?: params.shadowElevation
    val st = specularTop ?: params.specularTop
    Box(
        modifier = modifier
            .clip(shape)
            .let { mod ->
                if (se > 0f && sc != Color.Transparent) {
                    mod.shadow(se.dp, shape, clip = false, ambientColor = sc, spotColor = sc)
                } else mod
            }
            .border(BorderStroke(ClearDuDimens.GlassBorderWidth, bd), shape),
        contentAlignment = Alignment.Center,
    ) {
        // 模糊背景层 — 轻微模糊使背景图与卡片不重叠
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(ClearDuDimens.GlassBlurRadius)
                .drawBehindFill(bg)
                .drawSpecularOverlay(st)
        )
        // 清晰内容层
        content()
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