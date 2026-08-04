package com.cleardu.app.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 今日提醒卡片 —— 提醒中心页。
 *
 * 复刻参考 HTML 的 `.today-card`：
 * - 浅色玻璃容器，20dp 圆角，16dp 内边距
 * - 提醒项：脉冲圆点 + 时间 + 标题 + 状态徽章
 * - 圆点 8×8dp MedicalBlue，2s ease-in-out 脉冲（透明度 1→0.6，缩放 1→0.85）
 * - 圆点外发光：radialGradient(LightTintBlueGlow → Transparent)
 * - 时间列固定宽 44dp，等宽字体；状态徽章 LightTintCyanBg 底色 + MedicalBlue 文字
 * - 条目之间用 1dp LightDividerSubtle 分隔线（最后一条无分隔线）
 * - 空列表时显示提示文案，引导用户在用药/测量页面设置提醒
 *
 * @param reminders 今日提醒列表，由外部（如用药页面）设置后动态填充
 */
@Composable
fun ReminderTodayList(
    reminders: List<TodayReminder> = emptyList(),
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.ReminderTodayCardRadius),
        background = LiquidGlassColors.LightGlassBg,
        border = LiquidGlassColors.LightGlassBorder,
        specularTop = LiquidGlassColors.LightGlassSpecularTop
    ) {
        if (reminders.isEmpty()) {
            // 空状态：引导用户设置提醒
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ClearDuDimens.ReminderTodayCardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "暂无提醒",
                    style = ClearDuTypography.ReminderTodayText,
                    color = LiquidGlassColors.Text400,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "请在用药、测量等页面设置提醒",
                    style = ClearDuTypography.ReminderTodayStatus,
                    color = LiquidGlassColors.Text400.copy(alpha = 0.6f)
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(ClearDuDimens.ReminderTodayCardPadding)
            ) {
                reminders.forEachIndexed { index, item ->
                    TodayReminderRow(item)
                    if (index < reminders.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(LiquidGlassColors.LightDividerSubtle)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayReminderRow(item: TodayReminder) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ClearDuDimens.ReminderTodayItemPaddingV),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.ReminderTodayItemGap)
    ) {
        // 脉冲圆点 + 外发光
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            // 发光层：radialGradient(LightTintBlueGlow → Transparent)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val glowRadius = size.minDimension / 2f
                        val glowCenter = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    LiquidGlassColors.LightTintBlueGlow,
                                    Color.Transparent
                                ),
                                center = glowCenter,
                                radius = glowRadius
                            ),
                            radius = glowRadius,
                            center = glowCenter
                        )
                    }
            )
            // 圆点本体：8×8dp MedicalBlue，脉冲动画
            Box(
                modifier = Modifier
                    .size(ClearDuDimens.ReminderTodayDotSize)
                    .scale(dotScale)
                    .alpha(dotAlpha)
                    .background(LiquidGlassColors.MedicalBlue, CircleShape)
            )
        }
        // 时间列
        Text(
            text = item.time,
            style = ClearDuTypography.ReminderTodayTime,
            color = LiquidGlassColors.Text400,
            modifier = Modifier.width(ClearDuDimens.ReminderTodayTimeWidth)
        )
        // 标题
        Text(
            text = item.title,
            style = ClearDuTypography.ReminderTodayText,
            color = LiquidGlassColors.LightForeground,
            modifier = Modifier.weight(1f)
        )
        // 状态徽章
        Box(
            modifier = Modifier
                .background(LiquidGlassColors.LightTintCyanBg, CircleShape)
                .padding(
                    horizontal = ClearDuDimens.ReminderTodayStatusPaddingH,
                    vertical = ClearDuDimens.ReminderTodayStatusPaddingV
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.status,
                style = ClearDuTypography.ReminderTodayStatus,
                color = LiquidGlassColors.MedicalBlue
            )
        }
    }
}

data class TodayReminder(
    val time: String,
    val title: String,
    val status: String
)
