package com.cleardu.app.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 下次透析倒计时主卡片。
 *
 * 浅色强玻璃容器内展示「天 / 小时 / 分」倒计时，数字采用 MedicalCyan 等宽
 * 字体并附带 3s 呼吸光晕（透明度 1 ↔ 0.95 + 蓝色径向辉光 0.2 ↔ 0.35）；
 * 底部为「导航到医院」药丸按钮，按压缩放 0.95。
 *
 * 卡片底部 16dp 间距由父布局负责，本组件不额外添加。
 *
 * @param onNavigate 点击「导航到医院」回调
 */
@Composable
fun ReminderCountdownCard(
    onNavigate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 呼吸动画：3s ease-in-out，透明度 1 ↔ 0.95
    val infiniteTransition = rememberInfiniteTransition(label = "countdown")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    // 蓝色光晕：3s 呼吸，alpha 0.2 ↔ 0.35（rgba(0,122,255,0.2)→0.35）
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "countdownGlowAlpha"
    )

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.ReminderCountdownCardRadius),
        background = LiquidGlassColors.LightGlassBgStrong,
        border = LiquidGlassColors.LightGlassBorder,
        specularTop = LiquidGlassColors.LightGlassSpecularTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClearDuDimens.ReminderCountdownCardPadding)
        ) {
            // 1. 标签「下次透析」
            Text(
                text = "下次透析",
                style = ClearDuTypography.ReminderCountdownLabel,
                color = LiquidGlassColors.Text400,
                modifier = Modifier.padding(bottom = ClearDuDimens.ReminderCountdownLabelBottomMargin)
            )

            // 2. 倒计时行（圆形呼吸光晕 + 透明度脉动）
            Box(
                modifier = Modifier
                    .drawBehind {
                        val glowCenter = Offset(size.width / 2f, size.height / 2f)
                        val glowRadius = size.minDimension * 0.7f
                        drawCircle(
                            color = LiquidGlassColors.MedicalBlue.copy(alpha = glowAlpha * 0.6f),
                            radius = glowRadius,
                            center = glowCenter
                        )
                    }
                    .alpha(pulseAlpha)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "2",
                        style = ClearDuTypography.ReminderCountdownNumber,
                        color = LiquidGlassColors.MedicalCyan,
                        modifier = Modifier.padding(end = ClearDuDimens.ReminderCountdownNumberGap)
                    )
                    Text(
                        text = "天",
                        style = ClearDuTypography.ReminderCountdownUnit,
                        color = LiquidGlassColors.Text400,
                        modifier = Modifier.padding(end = ClearDuDimens.ReminderCountdownUnitEndMargin)
                    )
                    Text(
                        text = "14",
                        style = ClearDuTypography.ReminderCountdownNumber,
                        color = LiquidGlassColors.MedicalCyan,
                        modifier = Modifier.padding(end = ClearDuDimens.ReminderCountdownNumberGap)
                    )
                    Text(
                        text = "小时",
                        style = ClearDuTypography.ReminderCountdownUnit,
                        color = LiquidGlassColors.Text400,
                        modifier = Modifier.padding(end = ClearDuDimens.ReminderCountdownUnitEndMargin)
                    )
                    Text(
                        text = "32",
                        style = ClearDuTypography.ReminderCountdownNumber,
                        color = LiquidGlassColors.MedicalCyan,
                        modifier = Modifier.padding(end = ClearDuDimens.ReminderCountdownNumberGap)
                    )
                    Text(
                        text = "分",
                        style = ClearDuTypography.ReminderCountdownUnit,
                        color = LiquidGlassColors.Text400
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 3. 日期
            Text(
                text = "8月2日 周日 上午8:00",
                style = ClearDuTypography.ReminderCountdownDate,
                color = LiquidGlassColors.LightForeground,
                modifier = Modifier.padding(bottom = ClearDuDimens.ReminderCountdownDateBottomMargin)
            )

            // 4. 医院
            Text(
                text = "xx医院 血液净化中心",
                style = ClearDuTypography.ReminderCountdownHospital,
                color = LiquidGlassColors.Text400,
                modifier = Modifier.padding(bottom = ClearDuDimens.ReminderCountdownHospitalBottomMargin)
            )

            // 5. 导航按钮
            NavigatePillButton(onClick = onNavigate)
        }
    }
}

/**
 * 「导航到医院」药丸按钮。
 *
 * 圆角胶囊形状（CircleShape），浅青色 tint 背景 + 边框，按压缩放 0.95，
 * iOS 标准缓动 cubic-bezier(0.32, 0.72, 0, 1)。
 */
@Composable
private fun NavigatePillButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
        ),
        label = "navBtnScale"
    )

    Row(
        modifier = Modifier
            .scale(scale)
            .background(LiquidGlassColors.LightTintCyanBg, CircleShape)
            .border(1.dp, LiquidGlassColors.LightTintCyanBorder, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = ClearDuDimens.ReminderNavBtnPaddingH,
                vertical = ClearDuDimens.ReminderNavBtnPaddingV
            ),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReminderPinIcon(
            tint = LiquidGlassColors.MedicalBlue,
            modifier = Modifier.size(ClearDuDimens.ReminderNavBtnIconSize)
        )
        Text(
            text = "导航到医院",
            style = ClearDuTypography.ReminderNavBtn,
            color = LiquidGlassColors.MedicalBlue
        )
    }
}
