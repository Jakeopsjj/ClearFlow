package com.cleardu.app.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.ui.components.GlassCard
import com.cleardu.app.ui.components.MeshGradientBackground
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 个人资料页面 — 匹配 "个人资料" HTML 参考设计。
 *
 * Sections:
 *  - Avatar section (large avatar, edit button, name, patient ID)
 *  - 基本信息: name, gender, birth date, height, dry weight, blood type
 *  - 透析信息: dialysis type, first dialysis date, dialysis age, vascular access, hospital, schedule
 *  - 紧急联系: name, relation, phone
 *  - Save button
 */
@Composable
fun ProfileScreen(
    healthDataManager: HealthDataManager,
    onNavigateBack: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val settings by healthDataManager.settings.collectAsState(initial = null)
    val s = settings

    MeshGradientBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, end = 20.dp, top = 44.dp, bottom = 72.dp)
        ) {
            // === Page Nav ===
            PageNav(
                title = "个人资料",
                backLabel = "设置",
                onBackClick = onNavigateBack
            )

            Spacer(Modifier.height(8.dp))

            // === Avatar Section ===
            AvatarSection()

            Spacer(Modifier.height(28.dp))

            // === Section: 基本信息 ===
            ProfileSectionHeader("基本信息")
            ProfileCard {
                ProfileNavItem(label = "姓名", value = "张先生")
                ProfileNavItem(label = "性别", value = "男")
                ProfileNavItem(label = "出生日期", value = "1968-05-12（57岁）")
                ProfileNavItem(label = "身高", value = "172 cm")
                ProfileNavItem(
                    label = "干体重",
                    value = s?.dryWeightTarget ?: "65.0 kg",
                    highlight = true
                )
                ProfileNavItem(label = "血型", value = "A型 Rh阳性")
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 透析信息 ===
            ProfileSectionHeader("透析信息")
            ProfileCard {
                ProfileNavItem(label = "透析类型", value = "血液透析")
                ProfileNavItem(label = "首次透析日期", value = "2023-03-15")
                ProfileNavItem(label = "透析龄", value = "3年2个月")
                ProfileNavItem(label = "血管通路", value = "左前臂动静脉内瘘")
                ProfileNavItem(label = "透析医院", value = s?.hospitalName?.ifEmpty { "市第一人民医院" } ?: "市第一人民医院")
                ProfileNavItem(
                    label = "透析计划",
                    value = s?.dialysisPlan ?: "每周一三五 · 08:00",
                    subValue = "每次4小时"
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 紧急联系 ===
            ProfileSectionHeader("紧急联系")
            ProfileCard {
                EmergencyContactItem(
                    name = s?.emergencyContactName?.ifEmpty { "张女士" } ?: "张女士",
                    relation = "配偶",
                    phone = s?.emergencyContactPhone?.ifEmpty { "138-1234-5678" } ?: "138-1234-5678"
                )
                EmergencyContactItem(
                    name = "张小明",
                    relation = "儿子",
                    phone = "139-8765-4321",
                    isLast = true
                )
            }

            Spacer(Modifier.height(32.dp))

            // === Save Button ===
            SaveButton(onClick = {
                onNavigateToDashboard()
            })
        }
    }
}

// ===== Sub-components =====

@Composable
private fun PageNav(
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
            .padding(bottom = 24.dp, top = 4.dp)
    ) {
        // Back button
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
            // Back arrow icon
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
            Text(
                text = backLabel,
                fontSize = 17.sp,
                color = LiquidGlassColors.MedicalCyan
            )
        }

        // Title (centered)
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
private fun AvatarSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large avatar with edit button
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
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
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Edit button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(LiquidGlassColors.GlassBgStrong)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(14.dp)) {
                    val w = size.width; val h = size.height
                    val path = Path().apply {
                        moveTo(w * 0.72f, h * 0.15f)
                        lineTo(w * 0.85f, h * 0.28f)
                        lineTo(w * 0.5f, h * 0.63f)
                        lineTo(w * 0.37f, h * 0.63f)
                        lineTo(w * 0.37f, h * 0.5f)
                        close()
                    }
                    drawPath(path, color = LiquidGlassColors.Foreground, style = Stroke(width = 1.3f * density))
                    drawLine(
                        LiquidGlassColors.Foreground,
                        start = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.25f),
                        end = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.38f),
                        strokeWidth = 1.3f * density
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "张先生",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = LiquidGlassColors.Foreground,
            letterSpacing = (-0.03).sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "患者ID: DC202403001",
            fontSize = 14.sp,
            color = LiquidGlassColors.Text400
        )
    }
}

@Composable
private fun ProfileSectionHeader(title: String) {
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
private fun ProfileCard(
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
private fun ProfileNavItem(
    label: String,
    value: String,
    subValue: String? = null,
    highlight: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.08f else 0f,
        animationSpec = tween(150),
        label = "itemBg"
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
            color = if (highlight) LiquidGlassColors.MedicalCyan else LiquidGlassColors.Foreground,
            letterSpacing = (-0.01).sp,
            modifier = Modifier.weight(1f)
        )

        if (subValue != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = value,
                    fontSize = 15.sp,
                    color = if (highlight) LiquidGlassColors.MedicalCyan else LiquidGlassColors.Foreground,
                    letterSpacing = (-0.01).sp
                )
                Text(
                    text = subValue,
                    fontSize = 13.sp,
                    color = LiquidGlassColors.Text400
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 15.sp,
                color = if (highlight) LiquidGlassColors.MedicalCyan else LiquidGlassColors.Text400,
                letterSpacing = (-0.01).sp,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Spacer(Modifier.width(4.dp))

        // Chevron
        Text(
            text = "›",
            fontSize = 18.sp,
            color = LiquidGlassColors.Text400,
            modifier = Modifier.padding(end = 0.dp)
        )
    }
}

@Composable
private fun EmergencyContactItem(
    name: String,
    relation: String,
    phone: String,
    isLast: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.08f else 0f,
        animationSpec = tween(150),
        label = "contactBg"
    )

    Column(
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LiquidGlassColors.TintCyanBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LiquidGlassColors.MedicalCyan
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = LiquidGlassColors.Foreground,
                        letterSpacing = (-0.01).sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = relation,
                        fontSize = 13.sp,
                        color = LiquidGlassColors.Text400
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = phone,
                    fontSize = 14.sp,
                    color = LiquidGlassColors.MedicalCyan,
                    letterSpacing = (-0.01).sp
                )
            }

            // Call button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(LiquidGlassColors.TintGreenBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
                    val w = size.width; val h = size.height
                    drawArc(
                        LiquidGlassColors.MedicalGreen,
                        startAngle = 225f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.15f),
                        size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.7f),
                        style = Stroke(width = 1.5f * density)
                    )
                    drawLine(
                        LiquidGlassColors.MedicalGreen,
                        start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.68f),
                        end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.32f),
                        strokeWidth = 1.5f * density
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(200),
        label = "saveBtnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(LiquidGlassColors.MedicalCyan, LiquidGlassColors.MedicalBlue)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "保存修改",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            letterSpacing = (-0.01).sp,
            textAlign = TextAlign.Center
        )
    }
}