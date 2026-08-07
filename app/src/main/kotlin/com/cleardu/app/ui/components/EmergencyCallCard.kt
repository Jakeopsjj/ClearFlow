package com.cleardu.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import com.cleardu.app.ui.theme.glassParams

/**
 * 紧急呼叫卡片。
 *
 * 自定义红色玻璃容器，内含「紧急呼叫」标题、联系人姓名与电话、
 * 「立即拨打」渐变按钮（MedicalRed → DestructiveLight），
 * 以及底部居中的「家人联系」入口。
 *
 * Contact info is now configurable — tapping the contact area opens the
 * contact picker dialog.
 *
 * @param contactName 联系人姓名
 * @param contactPhone 联系人电话
 * @param onContactClick 点击联系人区域回调（打开联系人选择器）
 * @param onCall 点击「立即拨打」回调
 * @param onFamilyContact 点击「家人联系」回调
 */
@Composable
fun EmergencyCallCard(
    contactName: String = "",
    contactPhone: String = "",
    onContactClick: () -> Unit = {},
    onCall: () -> Unit = {},
    onFamilyContact: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(ClearDuDimens.ReminderEmergencyCardRadius)

    val displayContact = contactName.ifBlank { "点击设置紧急联系人" }
    val displayPhone = contactPhone.ifBlank { "" }
    val glass = glassParams()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        background = LiquidGlassColors.LightTintRedBg,
        border = LiquidGlassColors.LightTintRedBorder,
        shadowColor = glass.shadowColor,
        shadowElevation = glass.shadowElevation,
        specularTop = glass.specularTop
    ) {
        Box(modifier = Modifier.padding(ClearDuDimens.ReminderEmergencyCardPadding)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. 标题行
            Row(
                modifier = Modifier.padding(
                    bottom = if (contactName.isNotBlank()) ClearDuDimens.ReminderEmergencyTitleBottomMargin
                    else 8.dp
                ),
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

            // 2. 联系人（可点击编辑）
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onContactClick)
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = displayContact,
                        style = ClearDuTypography.ReminderEmergencyContact,
                        color = if (contactName.isNotBlank()) LiquidGlassColors.LightForeground else LiquidGlassColors.MedicalRed,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = if (contactName.isNotBlank()) ClearDuDimens.ReminderEmergencyContactBottomMargin else 0.dp)
                    )
                    if (displayPhone.isNotEmpty()) {
                        Text(
                            text = displayPhone,
                            style = ClearDuTypography.ReminderEmergencyPhone,
                            color = LiquidGlassColors.LightForeground,
                            modifier = Modifier.padding(bottom = ClearDuDimens.ReminderEmergencyPhoneBottomMargin)
                        )
                    }
                }
                if (contactName.isBlank()) {
                    ReminderPhoneIcon(
                        tint = LiquidGlassColors.MedicalRed,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            if (contactName.isBlank()) {
                Spacer(Modifier.height(ClearDuDimens.ReminderEmergencyPhoneBottomMargin))
            }

            // 4. 立即拨打按钮
            EmergencyCallButton(
                onClick = onCall,
                enabled = contactPhone.isNotBlank()
            )

            // 5. 家人联系
            FamilyContactLink(onClick = onFamilyContact)
        }
    }
    }
}

/**
 * 「立即拨打」全宽渐变按钮。
 */
@Composable
private fun EmergencyCallButton(onClick: () -> Unit, enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
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
                        colors = if (enabled) listOf(
                            LiquidGlassColors.MedicalRed,
                            LiquidGlassColors.DestructiveLight
                        ) else listOf(
                            LiquidGlassColors.Text400.copy(alpha = 0.3f),
                            LiquidGlassColors.Text400.copy(alpha = 0.3f)
                        )
                    )
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
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
                tint = if (enabled) LiquidGlassColors.White else LiquidGlassColors.Text400,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (enabled) "立即拨打" else "请先设置联系人",
                style = ClearDuTypography.ReminderEmergencyCallBtn,
                color = if (enabled) LiquidGlassColors.White else LiquidGlassColors.Text400
            )
        }
    }
}

/**
 * 「家人联系」居中入口。
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