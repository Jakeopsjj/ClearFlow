package com.cleardu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * iOS 风格 Toggle 开关，用于提醒中心页设置项。
 *
 * 复刻参考 HTML 的 `.toggle` 组件：
 * - 51 × 31 dp 胶囊形轨道，圆角 = height/2（CircleShape）
 * - 开启色 #5ac8fa（MedicalCyan），关闭色 rgba(120,120,128,0.32)
 * - 白色圆球 27 × 27 dp，按下时横向拉伸至 33 dp（×1.22），圆角随之变为胶囊形
 * - 按下且开启时圆球位置左移至 16 dp（translateX(14) + left(2)），避免视觉溢出
 * - 0.3s cubic-bezier(0.32, 0.72, 0, 1) 缓动，对应 iOS 标准交互曲线
 * - 圆球带 0 3px 8px / 0 1px 2px 阴影
 *
 * 位置矩阵：
 * | 状态         | left | width |
 * |-------------|------|-------|
 * | 未按下·关    | 2dp  | 27dp  |
 * | 未按下·开    | 22dp | 27dp  |
 * | 按下·关      | 2dp  | 33dp  |
 * | 按下·开      | 16dp | 33dp  |
 *
 * @param checked 当前开关状态
 * @param onCheckedChange 状态变更回调
 */
@Composable
fun ReminderToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // iOS 标准缓动：cubic-bezier(0.32, 0.72, 0, 1)
    val iosEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

    // 轨道背景色：关闭 → 开启
    val trackColor by animateColorAsState(
        targetValue = if (checked) LiquidGlassColors.ToggleOn else LiquidGlassColors.ToggleOff,
        animationSpec = tween(durationMillis = 300, easing = iosEasing),
        label = "trackColor"
    )

    // 圆球水平位置（left offset）
    val thumbOffset by animateDpAsState(
        targetValue = when {
            isPressed && checked -> 16.dp   // 按下·开：translateX(14) + left(2) = 16
            checked -> 2.dp + ClearDuDimens.ToggleThumbOffset // 未按下·开：translateX(20) + left(2) = 22
            else -> 2.dp                     // 关：left(2)
        },
        animationSpec = tween(durationMillis = 300, easing = iosEasing),
        label = "thumbOffset"
    )

    // 圆球宽度：按下时拉伸 ×1.22（27 → ~33dp）
    val thumbWidth by animateDpAsState(
        targetValue = if (isPressed) {
            ClearDuDimens.ToggleThumbSize * ClearDuDimens.ToggleThumbPressScale
        } else {
            ClearDuDimens.ToggleThumbSize
        },
        animationSpec = tween(durationMillis = 300, easing = iosEasing),
        label = "thumbWidth"
    )

    // 圆球形状：percent=50 在方形时为正圆，在 33×27 时为胶囊形（圆角自动钳制为 13.5dp）
    val thumbShape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .width(ClearDuDimens.ToggleWidth)
            .height(ClearDuDimens.ToggleHeight)
            .background(color = trackColor, shape = CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = 2.dp)
                .size(width = thumbWidth, height = ClearDuDimens.ToggleThumbSize)
                .shadow(elevation = 3.dp, shape = thumbShape, clip = true)
                .background(color = LiquidGlassColors.ToggleThumb)
        )
    }
}
