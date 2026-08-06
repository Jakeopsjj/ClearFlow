package com.cleardu.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleardu.app.ui.components.GlassCard
import com.cleardu.app.ui.components.MeshGradientBackground
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Settings page — matches the "设置" HTML reference design.
 *
 * Sections:
 *  - User profile card (navigates to profile)
 *  - 通用: dark mode, notifications, units, language
 *  - 数据与隐私: backup, export, permissions
 *  - 健康管理: dialysis plan, dry weight, fluid reminder, emergency contact
 *  - 关于: check update, privacy policy, about
 *  - 危险操作: clear cache, logout
 */
@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
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
            // === Page Header ===
            Text(
                text = "设置",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidGlassColors.Foreground,
                letterSpacing = (-0.03).sp,
                modifier = Modifier.padding(bottom = 24.dp, top = 4.dp)
            )

            // === User Profile Card ===
            UserProfileCard(onClick = onNavigateToProfile)

            Spacer(Modifier.height(28.dp))

            // === Section: 通用 ===
            SettingsSectionHeader("通用")
            SettingsCard {
                DarkModeToggleItem()
                SettingsNavItem(
                    icon = { NotificationIcon() },
                    iconBg = LiquidGlassColors.TintOrangeBg,
                    iconFg = LiquidGlassColors.MedicalOrange,
                    label = "通知与提醒",
                    sublabel = "强提醒已开启",
                    onClick = onNavigateToNotification
                )
                SettingsNavItem(
                    icon = { UnitIcon() },
                    iconBg = LiquidGlassColors.TintCyanBg,
                    iconFg = LiquidGlassColors.MedicalCyan,
                    label = "单位设置",
                    value = "kg/mmHg",
                    onClick = {}
                )
                SettingsNavItem(
                    icon = { LanguageIcon() },
                    iconBg = LiquidGlassColors.TintBlueBg,
                    iconFg = LiquidGlassColors.MedicalBlue,
                    label = "语言",
                    value = "简体中文",
                    onClick = {}
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 数据与隐私 ===
            SettingsSectionHeader("数据与隐私")
            SettingsCard {
                SettingsNavItem(
                    icon = { BackupIcon() },
                    iconBg = LiquidGlassColors.TintGreenBg,
                    iconFg = LiquidGlassColors.MedicalGreen,
                    label = "数据备份",
                    sublabel = "上次备份: 今天 08:30",
                    onClick = onNavigateToBackup
                )
                SettingsNavItem(
                    icon = { ExportIcon() },
                    iconBg = LiquidGlassColors.TintIndigoBg,
                    iconFg = LiquidGlassColors.MedicalIndigo,
                    label = "数据导出",
                    value = "PDF/Excel",
                    onClick = {}
                )
                SettingsNavItem(
                    icon = { LockIcon() },
                    iconBg = LiquidGlassColors.TintOrangeBg,
                    iconFg = LiquidGlassColors.MedicalOrange,
                    label = "权限管理",
                    onClick = {}
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 健康管理 ===
            SettingsSectionHeader("健康管理")
            SettingsCard {
                SettingsNavItem(
                    icon = { CalendarIcon() },
                    iconBg = LiquidGlassColors.TintCyanBg,
                    iconFg = LiquidGlassColors.MedicalCyan,
                    label = "透析计划",
                    sublabel = "每周一三五 08:00",
                    onClick = {}
                )
                SettingsNavItem(
                    icon = { WeightIcon() },
                    iconBg = LiquidGlassColors.TintGreenBg,
                    iconFg = LiquidGlassColors.MedicalGreen,
                    label = "干体重目标",
                    value = "65.0 kg",
                    onClick = {}
                )
                ToggleItem(
                    icon = { WaterDropIcon() },
                    iconBg = LiquidGlassColors.TintBlueBg,
                    iconFg = LiquidGlassColors.MedicalBlue,
                    label = "限水提醒",
                    initialChecked = true
                )
                SettingsNavItem(
                    icon = { EmergencyIcon() },
                    iconBg = LiquidGlassColors.TintRedBg,
                    iconFg = LiquidGlassColors.MedicalRed,
                    label = "紧急联系人",
                    value = "2位",
                    onClick = {}
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 关于 ===
            SettingsSectionHeader("关于")
            SettingsCard {
                SettingsNavItem(
                    icon = { UpdateIcon() },
                    iconBg = Color(0x14FFFFFF),
                    iconFg = LiquidGlassColors.Text400,
                    label = "检查更新",
                    value = "v2.1.0",
                    onClick = {}
                )
                SettingsNavItem(
                    icon = { DocIcon() },
                    iconBg = Color(0x14FFFFFF),
                    iconFg = LiquidGlassColors.Text400,
                    label = "用户协议与隐私政策",
                    onClick = {}
                )
                SettingsNavItem(
                    icon = { InfoIcon() },
                    iconBg = Color(0x14FFFFFF),
                    iconFg = LiquidGlassColors.Text400,
                    label = "关于清渡",
                    onClick = {}
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 危险操作 ===
            SettingsCard(modifier = Modifier.padding(bottom = 8.dp)) {
                SettingsTextItem(
                    icon = { TrashIcon() },
                    iconBg = LiquidGlassColors.TintRedBg,
                    iconFg = LiquidGlassColors.MedicalRed,
                    label = "清除缓存",
                    labelColor = LiquidGlassColors.MedicalRed,
                    value = "2.3 MB",
                    valueColor = LiquidGlassColors.MedicalRed
                )
            }
            SettingsCard {
                SettingsCenterItem(
                    label = "退出登录",
                    labelColor = LiquidGlassColors.MedicalRed
                )
            }
        }
    }
}

// ===== Sub-components =====

@Composable
private fun SettingsSectionHeader(title: String) {
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
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
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
private fun UserProfileCard(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(200),
        label = "userCardScale"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.16f else 0f,
        animationSpec = tween(200),
        label = "userCardBg"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            }
            .drawBehind {
                if (bgAlpha > 0f) {
                    drawRect(color = Color.White.copy(alpha = bgAlpha))
                }
            },
        shape = RoundedCornerShape(16.dp),
        background = Color.Transparent,
        border = Color.Transparent,
        shadowColor = Color.Transparent,
        shadowElevation = 0f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(LiquidGlassColors.MedicalCyan, LiquidGlassColors.MedicalBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "张",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.width(14.dp))

            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "张先生",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LiquidGlassColors.Foreground,
                    letterSpacing = (-0.02).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "血液透析 · 透析龄3年2个月",
                    fontSize = 13.sp,
                    color = LiquidGlassColors.Text400
                )
            }

            // Chevron
            Text(
                text = "›",
                fontSize = 20.sp,
                color = LiquidGlassColors.Text400,
                modifier = Modifier.alpha(0.6f)
            )
        }
    }
}

@Composable
private fun SettingsNavItem(
    icon: @Composable () -> Unit,
    iconBg: Color,
    iconFg: Color,
    label: String,
    sublabel: String? = null,
    value: String? = null,
    onClick: () -> Unit
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
            ) {
                isPressed = true
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(29.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(Modifier.width(12.dp))

        // Label group
        if (sublabel != null) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    color = LiquidGlassColors.Foreground,
                    letterSpacing = (-0.01).sp
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = sublabel,
                    fontSize = 13.sp,
                    color = LiquidGlassColors.Text400
                )
            }
        } else {
            Text(
                text = label,
                fontSize = 16.sp,
                color = LiquidGlassColors.Foreground,
                letterSpacing = (-0.01).sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Value
        if (value != null) {
            Text(
                text = value,
                fontSize = 15.sp,
                color = LiquidGlassColors.Text400,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        // Chevron
        Text(
            text = "›",
            fontSize = 18.sp,
            color = LiquidGlassColors.Text400,
            modifier = Modifier.alpha(0.6f)
        )
    }
}

@Composable
private fun ToggleItem(
    icon: @Composable () -> Unit,
    iconBg: Color,
    iconFg: Color,
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
        Box(
            modifier = Modifier
                .size(29.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            color = LiquidGlassColors.Foreground,
            letterSpacing = (-0.01).sp,
            modifier = Modifier.weight(1f)
        )

        IosToggle(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}

@Composable
private fun SettingsTextItem(
    icon: @Composable () -> Unit,
    iconBg: Color,
    iconFg: Color,
    label: String,
    labelColor: Color = LiquidGlassColors.Foreground,
    value: String? = null,
    valueColor: Color = LiquidGlassColors.Text400
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(29.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            color = labelColor,
            letterSpacing = (-0.01).sp,
            modifier = Modifier.weight(1f)
        )

        if (value != null) {
            Text(
                text = value,
                fontSize = 15.sp,
                color = valueColor,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

@Composable
private fun SettingsCenterItem(
    label: String,
    labelColor: Color = LiquidGlassColors.Foreground
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = labelColor,
            letterSpacing = (-0.01).sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DarkModeToggleItem() {
    var checked by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(29.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(LiquidGlassColors.TintPurpleBg),
            contentAlignment = Alignment.Center
        ) {
            DarkModeIcon()
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = "深色模式",
            fontSize = 16.sp,
            color = LiquidGlassColors.Foreground,
            letterSpacing = (-0.01).sp,
            modifier = Modifier.weight(1f)
        )

        IosToggle(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}

// ===== iOS-style Toggle Switch =====
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
                .background(
                    LiquidGlassColors.ToggleThumb,
                    shape = CircleShape
                )
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
        )
    }
}

// ===== Divider =====
@Composable
private fun SettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(0.5.dp)
            .background(Color.White.copy(alpha = 0.08f))
    )
}

// ===== Icons (inline SVG equivalents) =====

@Composable
private fun DarkModeIcon() {
    val color = LiquidGlassColors.MedicalPurple
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        drawCircle(
            color = color,
            radius = size.minDimension * 0.25f,
            center = Offset(size.width * 0.4f, size.height * 0.4f)
        )
        val path = Path().apply {
            moveTo(size.width * 0.4f, size.height * 0.05f)
            lineTo(size.width * 0.4f, size.height * 0.2f)
            moveTo(size.width * 0.4f, size.height * 0.8f)
            lineTo(size.width * 0.4f, size.height * 0.95f)
            moveTo(size.width * 0.05f, size.height * 0.4f)
            lineTo(size.width * 0.2f, size.height * 0.4f)
            moveTo(size.width * 0.8f, size.height * 0.4f)
            lineTo(size.width * 0.95f, size.height * 0.4f)
        }
        drawPath(path, color = color, style = Stroke(width = 1.5f * density))
    }
}

@Composable
private fun NotificationIcon() {
    val color = LiquidGlassColors.MedicalOrange
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.625f)
            lineTo(w * 0.25f, h * 0.47f)
            arcToRad(rect = androidx.compose.ui.geometry.Rect(
                w * 0.25f, h * 0.1f, w * 0.75f, h * 0.6f
            ), startAngleRadians = 0f, sweepAngleRadians = Math.PI.toFloat() * 2, forceMoveTo = false)
            lineTo(w * 0.75f, h * 0.625f)
            lineTo(w * 0.8125f, h * 0.71875f)
            lineTo(w * 0.1875f, h * 0.71875f)
            close()
        }
        drawPath(path, color = color, style = Stroke(width = 1.4f * density))
        drawLine(
            color = color, start = Offset(w * 0.406f, h * 0.78f),
            end = Offset(w * 0.594f, h * 0.78f),
            strokeWidth = 1.4f * density
        )
    }
}

@Composable
private fun UnitIcon() {
    val color = LiquidGlassColors.MedicalCyan
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        drawLine(color, Offset(w * 0.125f, h * 0.5f), Offset(w * 0.875f, h * 0.5f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.5f, h * 0.125f), Offset(w * 0.5f, h * 0.875f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.3125f, h * 0.3125f), Offset(w * 0.125f, h * 0.5f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.3125f, h * 0.6875f), Offset(w * 0.125f, h * 0.5f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.6875f, h * 0.3125f), Offset(w * 0.875f, h * 0.5f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.6875f, h * 0.6875f), Offset(w * 0.875f, h * 0.5f), strokeWidth = 1.4f * density)
    }
}

@Composable
private fun LanguageIcon() {
    val color = LiquidGlassColors.MedicalBlue
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        val r = w * 0.375f
        drawCircle(color, radius = r, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.4f * density))
        drawLine(color, Offset(w * 0.125f, h * 0.5f), Offset(w * 0.875f, h * 0.5f), strokeWidth = 1.2f * density)
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.125f)
            cubicTo(w * 0.625f, h * 0.5f, w * 0.625f, h * 0.875f, w * 0.5f, h * 0.875f)
            moveTo(w * 0.5f, h * 0.125f)
            cubicTo(w * 0.375f, h * 0.5f, w * 0.375f, h * 0.875f, w * 0.5f, h * 0.875f)
        }
        drawPath(path, color, style = Stroke(width = 1.2f * density))
    }
}

@Composable
private fun BackupIcon() {
    val color = LiquidGlassColors.MedicalGreen
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.625f)
            arcTo(rect = androidx.compose.ui.geometry.Rect(
                w * 0.25f, h * 0.375f, w * 0.75f, h * 0.625f
            ), startAngleDegrees = 0f, sweepAngleDegrees = 180f, forceMoveTo = false)
            lineTo(w * 0.75f, h * 0.75f)
            lineTo(w * 0.25f, h * 0.75f)
            close()
        }
        drawPath(path, color = color, style = Stroke(width = 1.4f * density))
        drawLine(color, Offset(w * 0.5f, h * 0.125f), Offset(w * 0.5f, h * 0.4375f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.375f, h * 0.3125f), Offset(w * 0.5f, h * 0.125f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.625f, h * 0.3125f), Offset(w * 0.5f, h * 0.125f), strokeWidth = 1.4f * density)
    }
}

@Composable
private fun ExportIcon() {
    val color = LiquidGlassColors.MedicalIndigo
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        drawLine(color, Offset(w * 0.5f, h * 0.125f), Offset(w * 0.5f, h * 0.625f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.3125f, h * 0.4375f), Offset(w * 0.5f, h * 0.625f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.6875f, h * 0.4375f), Offset(w * 0.5f, h * 0.625f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.1875f, h * 0.75f), Offset(w * 0.8125f, h * 0.75f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.1875f, h * 0.75f), Offset(w * 0.1875f, h * 0.875f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.8125f, h * 0.75f), Offset(w * 0.8125f, h * 0.875f), strokeWidth = 1.4f * density)
    }
}

@Composable
private fun LockIcon() {
    val color = LiquidGlassColors.MedicalOrange
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        drawRoundRect(color, topLeft = Offset(w * 0.1875f, h * 0.4375f), size = Size(w * 0.625f, h * 0.4375f), cornerRadius = CornerRadius(1.5f * density),
            style = Stroke(width = 1.4f * density))
        drawLine(color, Offset(w * 0.3125f, h * 0.4375f), Offset(w * 0.3125f, h * 0.3125f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.6875f, h * 0.4375f), Offset(w * 0.6875f, h * 0.3125f), strokeWidth = 1.4f * density)
        drawArc(color, 180f, 180f, false, topLeft = Offset(w * 0.3125f, h * 0.125f), size = Size(w * 0.375f, h * 0.375f),
            style = Stroke(width = 1.4f * density))
        drawCircle(color, w * 0.0625f * density, center = Offset(w * 0.5f, h * 0.656f))
    }
}

@Composable
private fun CalendarIcon() {
    val color = LiquidGlassColors.MedicalCyan
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        drawRoundRect(color, topLeft = Offset(w * 0.125f, h * 0.1875f), size = Size(w * 0.75f, h * 0.6875f), cornerRadius = CornerRadius(2f * density),
            style = Stroke(width = 1.4f * density))
        drawLine(color, Offset(w * 0.125f, h * 0.375f), Offset(w * 0.875f, h * 0.375f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.3125f, h * 0.125f), Offset(w * 0.3125f, h * 0.25f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.6875f, h * 0.125f), Offset(w * 0.6875f, h * 0.25f), strokeWidth = 1.4f * density)
        drawCircle(color, 0.8f * density, center = Offset(w * 0.343f, h * 0.594f))
    }
}

@Composable
private fun WeightIcon() {
    val color = LiquidGlassColors.MedicalGreen
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        drawRoundRect(color, topLeft = Offset(w * 0.125f, h * 0.3125f), size = Size(w * 0.75f, h * 0.5f), cornerRadius = CornerRadius(2f * density),
            style = Stroke(width = 1.4f * density))
        drawLine(color, Offset(w * 0.3125f, h * 0.3125f), Offset(w * 0.3125f, h * 0.25f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.6875f, h * 0.3125f), Offset(w * 0.6875f, h * 0.25f), strokeWidth = 1.4f * density)
        drawArc(color, 180f, 180f, false, topLeft = Offset(w * 0.3125f, h * 0.0625f), size = Size(w * 0.375f, h * 0.375f),
            style = Stroke(width = 1.4f * density))
        drawCircle(color, 1.2f * density, center = Offset(w * 0.5f, h * 0.5625f))
    }
}

@Composable
private fun WaterDropIcon() {
    val color = LiquidGlassColors.MedicalBlue
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.125f)
            cubicTo(w * 0.25f, h * 0.46875f, w * 0.25f, h * 0.71875f, w * 0.5f, h * 0.875f)
            cubicTo(w * 0.75f, h * 0.71875f, w * 0.75f, h * 0.46875f, w * 0.5f, h * 0.125f)
        }
        drawPath(path, color = color, style = Stroke(width = 1.4f * density))
        drawPath(path, color = color.copy(alpha = 0.1f))
    }
}

@Composable
private fun EmergencyIcon() {
    val color = LiquidGlassColors.MedicalRed
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        drawLine(color, Offset(w * 0.25f, h * 0.3125f), Offset(w * 0.75f, h * 0.3125f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.6875f, h * 0.3125f), Offset(w * 0.625f, h * 0.875f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.3125f, h * 0.3125f), Offset(w * 0.375f, h * 0.875f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.375f, h * 0.3125f), Offset(w * 0.375f, h * 0.25f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.625f, h * 0.3125f), Offset(w * 0.625f, h * 0.25f), strokeWidth = 1.4f * density)
        drawArc(color, 180f, 180f, false, topLeft = Offset(w * 0.375f, h * 0.0625f), size = Size(w * 0.25f, h * 0.375f),
            style = Stroke(width = 1.4f * density))
        drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.6875f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.40625f, h * 0.59375f), Offset(w * 0.59375f, h * 0.59375f), strokeWidth = 1.4f * density)
    }
}

@Composable
private fun UpdateIcon() {
    val color = LiquidGlassColors.Text400
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        drawCircle(color, w * 0.3125f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.4f * density))
        drawArc(color, 270f, 270f, false, topLeft = Offset(w * 0.1875f, h * 0.1875f), size = Size(w * 0.625f, h * 0.625f),
            style = Stroke(width = 1.4f * density))
        drawLine(color, Offset(w * 0.6875f, h * 0.25f), Offset(w * 0.8125f, h * 0.0625f), strokeWidth = 1.2f * density)
        drawLine(color, Offset(w * 0.3125f, h * 0.75f), Offset(w * 0.1875f, h * 0.9375f), strokeWidth = 1.2f * density)
        drawLine(color, Offset(w * 0.1875f, h * 0.3125f), Offset(w * 0.0625f, h * 0.125f), strokeWidth = 1.2f * density)
        drawLine(color, Offset(w * 0.8125f, h * 0.6875f), Offset(w * 0.9375f, h * 0.875f), strokeWidth = 1.2f * density)
    }
}

@Composable
private fun DocIcon() {
    val color = LiquidGlassColors.Text400
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * 0.1875f, h * 0.1875f)
            lineTo(w * 0.6875f, h * 0.1875f)
            lineTo(w * 0.8125f, h * 0.3125f)
            lineTo(w * 0.8125f, h * 0.8125f)
            lineTo(w * 0.1875f, h * 0.8125f)
            close()
        }
        drawPath(path, color = color, style = Stroke(width = 1.4f * density))
        drawLine(color, Offset(w * 0.6875f, h * 0.1875f), Offset(w * 0.6875f, h * 0.3125f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.6875f, h * 0.3125f), Offset(w * 0.8125f, h * 0.3125f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.3125f, h * 0.5f), Offset(w * 0.6875f, h * 0.5f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.3125f, h * 0.625f), Offset(w * 0.5625f, h * 0.625f), strokeWidth = 1.4f * density)
    }
}

@Composable
private fun InfoIcon() {
    val color = LiquidGlassColors.Text400
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        drawCircle(color, w * 0.375f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.4f * density))
        drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.6875f), strokeWidth = 1.8f * density)
        drawCircle(color, 0.5f * density, center = Offset(w * 0.5f, h * 0.375f))
    }
}

@Composable
private fun TrashIcon() {
    val color = LiquidGlassColors.MedicalRed
    androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width; val h = size.height
        drawLine(color, Offset(w * 0.1875f, h * 0.3125f), Offset(w * 0.8125f, h * 0.3125f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.6875f, h * 0.3125f), Offset(w * 0.625f, h * 0.875f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.3125f, h * 0.3125f), Offset(w * 0.375f, h * 0.875f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.375f, h * 0.3125f), Offset(w * 0.375f, h * 0.25f), strokeWidth = 1.4f * density)
        drawLine(color, Offset(w * 0.625f, h * 0.3125f), Offset(w * 0.625f, h * 0.25f), strokeWidth = 1.4f * density)
        drawArc(color, 180f, 180f, false, topLeft = Offset(w * 0.375f, h * 0.0625f), size = Size(w * 0.25f, h * 0.375f),
            style = Stroke(width = 1.4f * density))
    }
}