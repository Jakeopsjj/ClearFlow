package com.cleardu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 紧急呼叫卡片。
 *
 * 自定义红色玻璃容器（非 GlassCard），顶部高光 + 红色 tint 背景 + 红色边框，
 * 外层附加 2.5s 呼吸红色辉光（alpha 0.08 ↔ 0.20）。内含「紧急呼叫」标题、
 * 主治医生姓名与电话、「立即拨打」渐变按钮（MedicalRed → DestructiveLight），
 * 以及底部居中的「家人联系」入口。
 *
 * 卡片底部 16dp 间距由父布局负责，本组件不额外添加。
 *
 * @param onCall 点击「立即拨打」回调
 * @param onFamilyContact 点击「家人联系」回调
 */
@Composable
fun EmergencyCallCard(
    onCall: () -> Unit = {},
    onFamilyContact: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 红色呼吸辉光：2.5s ease-in-out，alpha 0.08 ↔ 0.20
    val infiniteTransition = rememberInfiniteTransition(label = "emergency")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val cardShape = RoundedCornerShape(ClearDuDimens.ReminderEmergencyCardRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // 呼吸红色径向辉光
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidGlassColors.LightTintRedGlow.copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.maxDimension
                    )
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .drawBehind {
                    // 红色 tint 背景
                    drawRect(LiquidGlassColors.LightTintRedBg)
                    // 顶部高光（上 50% 渐变）
                    val specBrush = Brush.verticalGradient(
                        colors = listOf(
                            LiquidGlassColors.LightGlassSpecularTop.copy(alpha = 0.65f),
                            LiquidGlassColors.LightGlassSpecularTop.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.5f
                    )
                    drawRect(specBrush)
                }
                .border(1.dp, LiquidGlassColors.LightTintRedBorder, cardShape)
                .padding(ClearDuDimens.ReminderEmergencyCardPadding)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. 标题行
                Row(
                    modifier = Modifier.padding(bottom = ClearDuDimens.ReminderEmergencyTitleBottomMargin),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReminderPhoneIcon(
                        tint = LiquidGlassColors.MedicalRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "紧急呼叫",
                        style = ClearDuTypography.ReminderEmergencyTitle,
                        color = LiquidGlassColors.MedicalRed
                    )
                }

                // 2. 联系人
                Text(
                    text = "主治医生",
                    style = ClearDuTypography.ReminderEmergencyContact,
                    color = LiquidGlassColors.LightForeground,
                    modifier = Modifier.padding(bottom = ClearDuDimens.ReminderEmergencyContactBottomMargin)
                )

                // 3. 电话号码
                Text(
                    text = "138-xxxx-xxxx",
                    style = ClearDuTypography.ReminderEmergencyPhone,
                    color = LiquidGlassColors.LightForeground,
                    modifier = Modifier.padding(bottom = ClearDuDimens.ReminderEmergencyPhoneBottomMargin)
                )

                // 4. 立即拨打按钮
                EmergencyCallButton(onClick = onCall)

                // 5. 家人联系
                FamilyContactLink(onClick = onFamilyContact)
            }
        }
    }
}

/**
 * 「立即拨打」全宽渐变按钮。
 *
 * 圆角 16dp，背景为 MedicalRed → DestructiveLight 线性渐变，按压缩放 0.97，
 * 附带红色投影。内容居中：电话图标 + 「立即拨打」白字。
 */
@Composable
private fun EmergencyCallButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
        ),
        label = "callBtnScale"
    )
    val btnShape = RoundedCornerShape(ClearDuDimens.ReminderEmergencyCallBtnRadius)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(4.dp, btnShape)
            .clip(btnShape)
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            LiquidGlassColors.MedicalRed,
                            LiquidGlassColors.DestructiveLight
                        )
                    )
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(ClearDuDimens.ReminderEmergencyCallBtnPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReminderPhoneIcon(
                tint = LiquidGlassColors.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "立即拨打",
                style = ClearDuTypography.ReminderEmergencyCallBtn,
                color = LiquidGlassColors.White
            )
        }
    }
}

/**
 * 「家人联系」居中入口。
 *
 * 默认 LightText600 灰色，按压时颜色过渡至 LightForeground，无 ripple。
 */
@Composable
private fun FamilyContactLink(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val color by animateColorAsState(
        targetValue = if (isPressed) LiquidGlassColors.LightForeground else LiquidGlassColors.LightText600,
        animationSpec = tween(durationMillis = 150),
        label = "familyLinkColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = ClearDuDimens.ReminderEmergencyFamilyTopMargin)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReminderUsersIcon(
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "家人联系",
            style = ClearDuTypography.ReminderFamilyLink,
            color = color
        )
    }
}
