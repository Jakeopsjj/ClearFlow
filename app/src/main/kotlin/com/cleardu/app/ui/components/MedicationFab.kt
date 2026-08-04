package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuMotion
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 用药页悬浮按钮（FAB）。
 *
 * 56dp 圆形玻璃按钮：蓝色发光底 + 顶部高光 + 白色加号图标。
 * 按下 0.92 缩放，无 ripple。
 */
@Composable
fun MedicationFab(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) ClearDuMotion.PermissionButtonPressScale else 1f,
        label = "medFabScale"
    )

    Box(
        modifier = modifier
            .size(ClearDuDimens.MedFabSize)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .drawBehind {
                // Outer glow (radial MedicalBlue aura)
                val center = Offset(size.width / 2f, size.height / 2f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidGlassColors.MedicalBlue.copy(alpha = 0.45f),
                            LiquidGlassColors.MedicalBlue.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.maxDimension / 2f
                    )
                )
                // Translucent glass fill
                drawRect(LiquidGlassColors.LightFabBg)
            }
            .drawWithContent {
                // Top specular highlight (top 50% white gradient)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * ClearDuDimens.GlassSpecularHeightFraction,
                        tileMode = TileMode.Clamp
                    )
                )
                drawContent()
            }
            .border(
                BorderStroke(ClearDuDimens.GlassBorderWidth, LiquidGlassColors.LightFabBorder),
                CircleShape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        MedPlusIcon(
            tint = LiquidGlassColors.White,
            modifier = Modifier.size(ClearDuDimens.MedFabIconSize)
        )
    }
}
