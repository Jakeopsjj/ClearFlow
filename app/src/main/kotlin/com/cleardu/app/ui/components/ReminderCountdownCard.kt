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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 下次透析倒计时主卡片。
 *
 * 浅色强玻璃容器内展示「天 / 小时 / 分」倒计时，数字采用 MedicalCyan 等宽
 * 字体并附带 3s 呼吸光晕；底部为「导航到医院」药丸按钮。
 *
 * Hospital name is now configurable — tapping the hospital area opens the
 * hospital picker dialog.
 *
 * @param hospitalName 当前设置的医院名称
 * @param hospitalAddress 医院地址
 * @param onHospitalClick 点击医院区域回调（打开医院选择器）
 * @param onNavigate 点击「导航到医院」回调
 */
@Composable
fun ReminderCountdownCard(
    hospitalName: String = "",
    hospitalAddress: String = "",
    onHospitalClick: () -> Unit = {},
    onNavigate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 呼吸动画
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

    val displayHospital = hospitalName.ifBlank { "点击设置透析医院" }
    val displayAddress = hospitalAddress.ifBlank { "未设置" }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ClearDuDimens.ReminderCountdownCardRadius),
        background = LiquidGlassColors.LightGlassBgStrong,
        border = LiquidGlassColors.LightGlassBorder,
        shadowColor = LiquidGlassColors.LightGlassShadow,
        shadowElevation = 8f,
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

            // 2. 倒计时行
            Row(modifier = Modifier.alpha(pulseAlpha)) {
                Text(
                    text = "2",
                    style = ClearDuTypography.ReminderCountdownNumber,
                    color = LiquidGlassColors.MedicalCyan,
                    modifier = Modifier.alignByBaseline().padding(end = ClearDuDimens.ReminderCountdownNumberGap)
                )
                Text(
                    text = "天",
                    style = ClearDuTypography.ReminderCountdownUnit,
                    color = LiquidGlassColors.Text400,
                    modifier = Modifier.alignByBaseline().padding(end = ClearDuDimens.ReminderCountdownUnitEndMargin)
                )
                Text(
                    text = "14",
                    style = ClearDuTypography.ReminderCountdownNumber,
                    color = LiquidGlassColors.MedicalCyan,
                    modifier = Modifier.alignByBaseline().padding(end = ClearDuDimens.ReminderCountdownNumberGap)
                )
                Text(
                    text = "小时",
                    style = ClearDuTypography.ReminderCountdownUnit,
                    color = LiquidGlassColors.Text400,
                    modifier = Modifier.alignByBaseline().padding(end = ClearDuDimens.ReminderCountdownUnitEndMargin)
                )
                Text(
                    text = "32",
                    style = ClearDuTypography.ReminderCountdownNumber,
                    color = LiquidGlassColors.MedicalCyan,
                    modifier = Modifier.alignByBaseline().padding(end = ClearDuDimens.ReminderCountdownNumberGap)
                )
                Text(
                    text = "分",
                    style = ClearDuTypography.ReminderCountdownUnit,
                    color = LiquidGlassColors.Text400,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Spacer(Modifier.height(8.dp))

            // 3. 日期
            Text(
                text = "8月2日 周日 上午8:00",
                style = ClearDuTypography.ReminderCountdownDate,
                color = LiquidGlassColors.LightForeground,
                modifier = Modifier.padding(bottom = ClearDuDimens.ReminderCountdownDateBottomMargin)
            )

            // 4. 医院（可点击编辑）
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onHospitalClick)
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = displayHospital,
                        style = ClearDuTypography.ReminderCountdownHospital,
                        color = if (hospitalName.isNotBlank()) LiquidGlassColors.Text400 else LiquidGlassColors.MedicalCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (displayAddress.isNotEmpty()) {
                        Text(
                            text = displayAddress,
                            style = ClearDuTypography.ReminderSettingDetail,
                            color = LiquidGlassColors.Text400.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (hospitalName.isBlank()) {
                    ReminderPinIcon(
                        tint = LiquidGlassColors.MedicalCyan,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(ClearDuDimens.ReminderCountdownHospitalBottomMargin))

            // 5. 导航按钮
            NavigatePillButton(
                onClick = onNavigate,
                enabled = hospitalName.isNotBlank()
            )
        }
    }
}

/**
 * 「导航到医院」药丸按钮。
 */
@Composable
private fun NavigatePillButton(onClick: () -> Unit, enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
        ),
        label = "navBtnScale"
    )

    Row(
        modifier = Modifier
            .scale(scale)
            .background(
                if (enabled) LiquidGlassColors.LightTintCyanBg else LiquidGlassColors.GlassBg,
                CircleShape
            )
            .border(
                1.dp,
                if (enabled) LiquidGlassColors.LightTintCyanBorder else LiquidGlassColors.GlassBorder,
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
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
            tint = if (enabled) LiquidGlassColors.MedicalBlue else LiquidGlassColors.Text400,
            modifier = Modifier.size(ClearDuDimens.ReminderNavBtnIconSize)
        )
        Text(
            text = if (enabled) "导航到医院" else "请先设置医院",
            style = ClearDuTypography.ReminderNavBtn,
            color = if (enabled) LiquidGlassColors.MedicalBlue else LiquidGlassColors.Text400
        )
    }
}