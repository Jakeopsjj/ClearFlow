package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Floating pill navigation bar.
 *
 * Reproduces `.nav-bar` from the reference:
 * - Floating pill shape with strong glass background
 * - Top specular highlight
 * - 5 nav items with indicators
 * - Active state with cyan indicator dot
 */
@Composable
fun FloatingNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val labels = listOf("首页", "记录", "数据", "用药", "提醒")

    Box(
        modifier = modifier
            .width(ClearDuDimens.NavBarWidth)
            .height(ClearDuDimens.NavBarHeight)
            .clip(RoundedCornerShape(ClearDuDimens.NavBarRadius))
            .drawBehind {
                // Glass background
                drawRect(color = LiquidGlassColors.NavBg)
            }
            .border(
                BorderStroke(0.5.dp, LiquidGlassColors.NavBorder),
                RoundedCornerShape(ClearDuDimens.NavBarRadius)
            )
            .drawBehind {
                // Top specular highlight
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        LiquidGlassColors.NavSpecular,
                        Color.Transparent.copy(alpha = 0.02f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.5f,
                    tileMode = TileMode.Clamp
                )
                drawRect(brush = brush)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = ClearDuDimens.NavBarPaddingH),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            labels.forEachIndexed { index, label ->
                NavItem(
                    label = label,
                    isActive = index == selectedIndex,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(350),
        label = "navIndicator"
    )

    Column(
        modifier = Modifier
            .size(ClearDuDimens.NavItemSize)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon placeholder (dot)
        Box(
            modifier = Modifier.size(ClearDuDimens.NavIconSize),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(
                    color = if (isActive) LiquidGlassColors.NavIconActive
                    else LiquidGlassColors.NavIconInactive,
                    radius = 6.dp.toPx()
                )
            }
        }

        // Indicator
        Box(
            modifier = Modifier
                .width(ClearDuDimens.NavIndicatorWidth)
                .height(ClearDuDimens.NavIndicatorHeight)
                .clip(RoundedCornerShape(ClearDuDimens.NavIndicatorRadius))
                .drawBehind {
                    drawRect(
                        color = LiquidGlassColors.NavIndicator.copy(alpha = indicatorAlpha)
                    )
                }
        )
    }
}