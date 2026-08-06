package com.cleardu.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleardu.app.ui.components.GlassCard
import com.cleardu.app.ui.components.MeshGradientBackground
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 通知与提醒页面 — 匹配 "通知与提醒" HTML 参考设计。
 *
 * Sections:
 *  - Strong reminder card (orange tinted glass card with icon, title, description, toggle)
 *  - 透析相关: dialysis day reminder, weight reminder, water restriction, water control
 *  - 用药提醒: medication reminder, EPO injection, iron supplement, missed dose reminder
 *  - 健康监测: BP measurement, abnormal data warning, weight gain warning, checkup reminder
 *  - 提醒方式: sound, vibration, lock screen popup, reminder time period
 */
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    MeshGradientBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, end = 20.dp, top = 44.dp, bottom = 72.dp)
        ) {
            // === Page Nav ===
            NotifPageNav(
                title = "通知与提醒",
                backLabel = "设置",
                onBackClick = onNavigateBack
            )

            Spacer(Modifier.height(4.dp))

            // === Strong Reminder Card ===
            StrongReminderCard()

            Spacer(Modifier.height(28.dp))

            // === Section: 透析相关 ===
            NotifSectionHeader("透析相关")
            NotifCard {
                NotifToggleWithSub(
                    label = "透析日提醒",
                    sublabel = "透析前1小时",
                    initialChecked = true
                )
                NotifToggleWithSub(
                    label = "称重提醒",
                    sublabel = "透析前后",
                    initialChecked = true
                )
                NotifToggleWithSub(
                    label = "限水提醒",
                    sublabel = "每日多次提醒",
                    initialChecked = true
                )
                NotifToggleItem(
                    label = "控水达标提醒",
                    initialChecked = false
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 用药提醒 ===
            NotifSectionHeader("用药提醒")
            NotifCard {
                NotifToggleWithSub(
                    label = "服药提醒",
                    sublabel = "按您设置的用药计划",
                    initialChecked = true
                )
                NotifToggleWithSub(
                    label = "注射促红素",
                    sublabel = "每周二、五 20:00",
                    initialChecked = true
                )
                NotifToggleItem(
                    label = "补铁剂提醒",
                    initialChecked = true
                )
                NotifToggleWithSub(
                    label = "用药漏服提醒",
                    sublabel = "15分钟后二次提醒",
                    initialChecked = true
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 健康监测 ===
            NotifSectionHeader("健康监测")
            NotifCard {
                NotifToggleWithSub(
                    label = "血压测量提醒",
                    sublabel = "每日早晚",
                    initialChecked = true
                )
                NotifToggleWithSub(
                    label = "异常数据预警",
                    sublabel = "血压/钾/磷超标时",
                    warning = true,
                    initialChecked = true
                )
                NotifToggleWithSub(
                    label = "体重增长过快警告",
                    sublabel = "日增重>1.5kg时",
                    initialChecked = true
                )
                NotifToggleWithSub(
                    label = "复查提醒",
                    sublabel = "每月一次",
                    initialChecked = true
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 提醒方式 ===
            NotifSectionHeader("提醒方式")
            NotifCard {
                NotifNavItem(label = "声音提醒", value = "默认铃声")
                NotifToggleItem(label = "震动提醒", initialChecked = true)
                NotifToggleWithSub(
                    label = "锁屏弹窗",
                    sublabel = "强提醒",
                    accentSub = true,
                    initialChecked = true
                )
                NotifNavItem(label = "提醒时段", value = "全天")
            }
        }
    }
}

// ===== Sub-components =====

@Composable
private fun NotifPageNav(
    title: String,
    backLabel: String,
    onBackClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(200),
        label = "backBtnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp, top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    onBackClick()
                }
                .padding(start = 0.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                val w = size.width; val h = size.height
                val path = Path().apply {
                    moveTo(w * 0.6f, h * 0.25f)
                    lineTo(w * 0.35f, h * 0.5f)
                    lineTo(w * 0.6f, h * 0.75f)
                }
                drawPath(path, color = LiquidGlassColors.MedicalCyan, style = Stroke(width = 2f * density))
            }
            Spacer(Modifier.width(4.dp))
            Text(text = backLabel, fontSize = 17.sp, color = LiquidGlassColors.MedicalCyan)
        }

        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = LiquidGlassColors.Foreground,
            letterSpacing = (-0.02).sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun StrongReminderCard() {
    var checked by remember { mutableStateOf(true) }
    val trackColor by animateColorAsState(
        targetValue = if (checked) LiquidGlassColors.MedicalOrange else LiquidGlassColors.ToggleOff,
        animationSpec = tween(300),
        label = "strongToggleTrack"
    )
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 22f else 0f,
        animationSpec = tween(300),
        label = "strongToggleThumb"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        background = LiquidGlassColors.TintOrangeBg,
        border = LiquidGlassColors.TintOrangeBorder,
        shadowColor = Color(0x26FF9500),
        shadowElevation = 6f,
        specularTop = Color(0x0FFFFFFF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bell alert icon
            androidx.compose.foundation.Canvas(modifier = Modifier.size(40.dp)) {
                val w = size.width; val h = size.height
                // Bell body
                val bellPath = Path().apply {
                    moveTo(w * 0.25f, h * 0.65f)
                    lineTo(w * 0.25f, h * 0.45f)
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(w * 0.25f, h * 0.1f, w * 0.75f, h * 0.6f),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 360f,
                        forceMoveTo = false
                    )
                    lineTo(w * 0.75f, h * 0.65f)
                    lineTo(w * 0.8125f, h * 0.75f)
                    lineTo(w * 0.1875f, h * 0.75f)
                    close()
                }
                drawPath(bellPath, color = LiquidGlassColors.MedicalOrange, style = Stroke(width = 2.2f * density))
                // Clapper
                drawLine(
                    LiquidGlassColors.MedicalOrange,
                    Offset(w * 0.5f, h * 0.78f),
                    Offset(w * 0.5f, h * 0.88f),
                    strokeWidth = 2.2f * density
                )
                drawCircle(
                    LiquidGlassColors.MedicalOrange,
                    radius = 1.5f * density,
                    center = Offset(w * 0.5f, h * 0.92f)
                )
                // Alert lines
                drawLine(LiquidGlassColors.MedicalOrange, Offset(w * 0.22f, h * 0.22f), Offset(w * 0.22f, h * 0.12f), strokeWidth = 1.8f * density)
                drawLine(LiquidGlassColors.MedicalOrange, Offset(w * 0.78f, h * 0.22f), Offset(w * 0.78f, h * 0.12f), strokeWidth = 1.8f * density)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "强提醒模式",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiquidGlassColors.Foreground,
                    letterSpacing = (-0.02).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "在锁屏和后台状态下全屏弹窗提醒您按时服药和透析",
                    fontSize = 13.sp,
                    color = LiquidGlassColors.Text400,
                    lineHeight = 18.sp
                )
            }

            // Large orange toggle
            Box(
                modifier = Modifier
                    .size(56.dp, 34.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(trackColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { checked = !checked }
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset.dp, y = 2.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(LiquidGlassColors.ToggleThumb, CircleShape)
                        .shadow(4.dp, CircleShape, ambientColor = Color(0x4DFF9500), spotColor = Color(0x33FF9500))
                )
            }
        }
    }
}

@Composable
private fun NotifSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = LiquidGlassColors.Text400,
        letterSpacing = 0.65.sp,
        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun NotifCard(
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorderSubtle,
        shadowColor = LiquidGlassColors.GlassShadow,
        shadowElevation = 4f,
        specularTop = Color(0x14FFFFFF)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun NotifToggleItem(
    label: String,
    initialChecked: Boolean = false
) {
    var checked by remember { mutableStateOf(initialChecked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = LiquidGlassColors.Foreground,
            letterSpacing = (-0.01).sp,
            modifier = Modifier.weight(1f)
        )
        IosToggle(checked = checked, onCheckedChange = { checked = it })
    }
}

@Composable
private fun NotifToggleWithSub(
    label: String,
    sublabel: String,
    warning: Boolean = false,
    accentSub: Boolean = false,
    initialChecked: Boolean = false
) {
    var checked by remember { mutableStateOf(initialChecked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = if (warning) LiquidGlassColors.MedicalRed else LiquidGlassColors.Foreground,
                letterSpacing = (-0.01).sp
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = sublabel,
                fontSize = 13.sp,
                color = when {
                    warning -> LiquidGlassColors.MedicalRed.copy(alpha = 0.7f)
                    accentSub -> LiquidGlassColors.MedicalOrange
                    else -> LiquidGlassColors.Text400
                }
            )
        }
        IosToggle(checked = checked, onCheckedChange = { checked = it })
    }
}

@Composable
private fun NotifNavItem(
    label: String,
    value: String
) {
    var isPressed by remember { mutableStateOf(false) }
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.08f else 0f,
        animationSpec = tween(150),
        label = "navItemBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (bgAlpha > 0f) {
                    drawRect(color = Color.White.copy(alpha = bgAlpha))
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { isPressed = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = LiquidGlassColors.Foreground,
            letterSpacing = (-0.01).sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = LiquidGlassColors.Text400,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = "›",
            fontSize = 18.sp,
            color = LiquidGlassColors.Text400
        )
    }
}

// ===== iOS-style Toggle =====
@Composable
private fun IosToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) LiquidGlassColors.MedicalGreen else LiquidGlassColors.ToggleOff,
        animationSpec = tween(300),
        label = "toggleTrack"
    )
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 20f else 0f,
        animationSpec = tween(300),
        label = "toggleThumb"
    )

    Box(
        modifier = modifier
            .size(51.dp, 31.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset.dp, y = 2.dp)
                .size(27.dp)
                .clip(CircleShape)
                .background(LiquidGlassColors.ToggleThumb, CircleShape)
                .shadow(3.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.25f))
        )
    }
}