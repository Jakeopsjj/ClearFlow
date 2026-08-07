package com.cleardu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.backgroundAwareColors

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
    modifier: Modifier = Modifier,
    lightMode: Boolean = false
) {
    val labels = listOf("首页", "记录", "数据", "用药", "提醒")

    val shape = RoundedCornerShape(ClearDuDimens.NavBarRadius)
    val navBg = if (lightMode) LiquidGlassColors.LightNavBg else LiquidGlassColors.NavBg
    val navBorder = if (lightMode) LiquidGlassColors.LightNavBorder else LiquidGlassColors.NavBorder
    val navSpecular = if (lightMode) LiquidGlassColors.LightNavSpecular else LiquidGlassColors.NavSpecular

    // Layered approach: blurred background + sharp content on top
    Box(modifier = modifier) {
        // === Layer 1: Real-time blurred glass background ===
        // This Box blurs the page content scrolling behind the nav bar
        // and draws a semi-transparent background on top.
        Box(
            modifier = Modifier
                .width(ClearDuDimens.NavBarWidth)
                .height(ClearDuDimens.NavBarHeight)
                .clip(shape)
                .blur(12.dp)
                .background(navBg.copy(alpha = 0.85f))
        )

        // === Layer 2: Sharp content (border, specular, nav items) ===
        Box(
            modifier = Modifier
                .width(ClearDuDimens.NavBarWidth)
                .height(ClearDuDimens.NavBarHeight)
                .clip(shape)
        ) {
            // Subtle top/bottom border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(BorderStroke(0.5.dp, navBorder), shape)
            )

            // Top specular highlight (glass reflection)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val brush = Brush.verticalGradient(
                            colors = listOf(
                                navSpecular,
                                Color.Transparent.copy(alpha = 0.02f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height * 0.5f,
                            tileMode = TileMode.Clamp
                        )
                        drawRect(brush = brush)
                    }
            )

            // Navigation items (rendered sharp above the blurred background)
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = ClearDuDimens.NavBarPaddingH),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                labels.forEachIndexed { index, label ->
                    NavItem(
                        index = index,
                        label = label,
                        isActive = index == selectedIndex,
                        onClick = { onItemSelected(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    index: Int,
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
        // Icon
        Box(
            modifier = Modifier.size(ClearDuDimens.NavIconSize),
            contentAlignment = Alignment.Center
        ) {
            val iconTint = if (isActive) LiquidGlassColors.NavIconActive else backgroundAwareColors().text400
            when (index) {
                0 -> NavHomeIcon(tint = iconTint)
                1 -> NavRecordIcon(tint = iconTint)
                2 -> NavDataIcon(tint = iconTint)
                3 -> NavMedicationIcon(tint = iconTint)
                4 -> NavRemindersIcon(tint = iconTint)
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
