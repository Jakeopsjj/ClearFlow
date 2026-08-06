package com.cleardu.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleardu.app.data.AppSettings
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.ui.components.GlassCard
import com.cleardu.app.ui.components.WeatherBackground
import com.cleardu.app.ui.theme.LiquidGlassColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// ===== Dialog type enum =====

private enum class EditDialogType { NONE, NAME, GENDER, BIRTH_DATE, HEIGHT, DRY_WEIGHT,
    BLOOD_TYPE, DIALYSIS_TYPE, FIRST_DIALYSIS_DATE, VASCULAR_ACCESS, HOSPITAL,
    DIALYSIS_PLAN, EMERGENCY_CONTACT, PATIENT_ID }

// ===== Blood type options =====

private val BLOOD_TYPE_OPTIONS = listOf(
    "A型 Rh阳性", "B型 Rh阳性", "AB型 Rh阳性", "O型 Rh阳性",
    "A型 Rh阴性", "B型 Rh阴性", "AB型 Rh阴性", "O型 Rh阴性"
)

// ===== Dialysis type options =====

private val DIALYSIS_TYPE_OPTIONS = listOf("血液透析", "腹膜透析")

/**
 * 个人资料页面 — 匹配 "个人资料" HTML 参考设计。
 *
 * Sections:
 *  - Avatar section (large avatar, edit button, name, patient ID)
 *  - 基本信息: name, gender, birth date, height, dry weight, blood type, patient ID
 *  - 透析信息: dialysis type, first dialysis date, dialysis age, vascular access, hospital, schedule
 *  - 紧急联系: name, relation, phone (with call button)
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val settings by healthDataManager.settings.collectAsState(initial = null)
    val s = settings ?: return

    // ---- Dialog state ----
    var activeDialog by remember { mutableStateOf(EditDialogType.NONE) }
    var editText by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(s.profileGender) }
    var selectedBloodType by remember { mutableStateOf(s.profileBloodType) }
    var selectedDialysisType by remember { mutableStateOf(s.profileDialysisType) }
    // Emergency contact editing state
    var ecName by remember { mutableStateOf("") }
    var ecPhone by remember { mutableStateOf("") }
    var ecRelation by remember { mutableStateOf("") }

    fun update(block: (AppSettings) -> AppSettings) {
        scope.launch { healthDataManager.updateSettings(block) }
    }

    fun dismissDialog() { activeDialog = EditDialogType.NONE }

    fun openTextDialog(type: EditDialogType, currentValue: String) {
        editText = currentValue
        activeDialog = type
    }

    fun openGenderDialog() {
        selectedGender = s.profileGender
        activeDialog = EditDialogType.GENDER
    }

    fun openBloodTypeDialog() {
        selectedBloodType = s.profileBloodType
        activeDialog = EditDialogType.BLOOD_TYPE
    }

    fun openDialysisTypeDialog() {
        selectedDialysisType = s.profileDialysisType
        activeDialog = EditDialogType.DIALYSIS_TYPE
    }

    fun openEmergencyContactDialog() {
        ecName = s.emergencyContactName.ifEmpty { "张女士" }
        ecPhone = s.emergencyContactPhone.ifEmpty { "138-1234-5678" }
        ecRelation = s.emergencyContactRelation
        activeDialog = EditDialogType.EMERGENCY_CONTACT
    }

    fun saveTextEdit() {
        val text = editText.trim()
        when (activeDialog) {
            EditDialogType.NAME -> {
                if (text.isEmpty()) {
                    Toast.makeText(context, "姓名不能为空", Toast.LENGTH_SHORT).show()
                    return
                }
                update { it.copy(profileName = text) }
            }
            EditDialogType.HEIGHT -> {
                update { it.copy(profileHeight = text) }
            }
            EditDialogType.BIRTH_DATE -> {
                update { it.copy(profileBirthDate = text) }
            }
            EditDialogType.DRY_WEIGHT -> {
                update { it.copy(dryWeightTarget = text) }
            }
            EditDialogType.FIRST_DIALYSIS_DATE -> {
                update { it.copy(profileFirstDialysisDate = text) }
            }
            EditDialogType.VASCULAR_ACCESS -> {
                update { it.copy(profileVascularAccess = text) }
            }
            EditDialogType.HOSPITAL -> {
                update { it.copy(hospitalName = text) }
            }
            EditDialogType.DIALYSIS_PLAN -> {
                update { it.copy(dialysisPlan = text) }
            }
            EditDialogType.PATIENT_ID -> {
                update { it.copy(profilePatientId = text) }
            }
            else -> {}
        }
        dismissDialog()
    }

    fun saveGender() {
        update { it.copy(profileGender = selectedGender) }
        dismissDialog()
    }

    fun saveBloodType() {
        update { it.copy(profileBloodType = selectedBloodType) }
        dismissDialog()
    }

    fun saveDialysisType() {
        update { it.copy(profileDialysisType = selectedDialysisType) }
        dismissDialog()
    }

    fun saveEmergencyContact() {
        if (ecName.isBlank()) {
            Toast.makeText(context, "联系人姓名不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        update {
            it.copy(
                emergencyContactName = ecName.trim(),
                emergencyContactPhone = ecPhone.trim(),
                emergencyContactRelation = ecRelation.trim()
            )
        }
        dismissDialog()
    }

    fun saveAll() {
        scope.launch {
            healthDataManager.updateSettings { it }
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
            onNavigateToDashboard()
        }
    }

    // ---- Compute dialysis age ----
    fun calcDialysisAge(): String {
        return try {
            val date = LocalDate.parse(s.profileFirstDialysisDate, DateTimeFormatter.ISO_LOCAL_DATE)
            val period = Period.between(date, LocalDate.now())
            "${period.years}年${period.months}个月"
        } catch (_: DateTimeParseException) {
            s.profileFirstDialysisDate
        }
    }

    // ---- Compute age from birth date ----
    fun calcAge(): String {
        return try {
            val date = LocalDate.parse(s.profileBirthDate, DateTimeFormatter.ISO_LOCAL_DATE)
            val period = Period.between(date, LocalDate.now())
            "${period.years}岁"
        } catch (_: DateTimeParseException) {
            ""
        }
    }

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
            AvatarSection(
                profileName = s.profileName,
                patientId = s.profilePatientId,
                onAvatarEditClick = {
                    // Placeholder for future avatar editing
                    Toast.makeText(context, "头像编辑功能即将上线", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(Modifier.height(28.dp))

            // === Section: 基本信息 ===
            ProfileSectionHeader("基本信息")
            ProfileCard {
                ProfileNavItem(
                    label = "姓名",
                    value = s.profileName,
                    onClick = { openTextDialog(EditDialogType.NAME, s.profileName) }
                )
                ProfileNavItem(
                    label = "性别",
                    value = s.profileGender,
                    onClick = { openGenderDialog() }
                )
                ProfileNavItem(
                    label = "出生日期",
                    value = "${s.profileBirthDate}（${calcAge()}）",
                    onClick = { openTextDialog(EditDialogType.BIRTH_DATE, s.profileBirthDate) }
                )
                ProfileNavItem(
                    label = "身高",
                    value = s.profileHeight,
                    onClick = { openTextDialog(EditDialogType.HEIGHT, s.profileHeight.replace(" cm", "")) }
                )
                ProfileNavItem(
                    label = "干体重",
                    value = s.dryWeightTarget,
                    highlight = true,
                    onClick = { openTextDialog(EditDialogType.DRY_WEIGHT, s.dryWeightTarget.replace(" kg", "")) }
                )
                ProfileNavItem(
                    label = "血型",
                    value = s.profileBloodType,
                    onClick = { openBloodTypeDialog() }
                )
                ProfileNavItem(
                    label = "患者ID",
                    value = s.profilePatientId,
                    onClick = { openTextDialog(EditDialogType.PATIENT_ID, s.profilePatientId) }
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 透析信息 ===
            ProfileSectionHeader("透析信息")
            ProfileCard {
                ProfileNavItem(
                    label = "透析类型",
                    value = s.profileDialysisType,
                    onClick = { openDialysisTypeDialog() }
                )
                ProfileNavItem(
                    label = "首次透析日期",
                    value = s.profileFirstDialysisDate,
                    onClick = { openTextDialog(EditDialogType.FIRST_DIALYSIS_DATE, s.profileFirstDialysisDate) }
                )
                ProfileNavItem(
                    label = "透析龄",
                    value = calcDialysisAge()
                )
                ProfileNavItem(
                    label = "血管通路",
                    value = s.profileVascularAccess,
                    onClick = { openTextDialog(EditDialogType.VASCULAR_ACCESS, s.profileVascularAccess) }
                )
                ProfileNavItem(
                    label = "透析医院",
                    value = s.hospitalName.ifEmpty { "市第一人民医院" },
                    onClick = { openTextDialog(EditDialogType.HOSPITAL, s.hospitalName.ifEmpty { "市第一人民医院" }) }
                )
                ProfileNavItem(
                    label = "透析计划",
                    value = s.dialysisPlan,
                    subValue = "每次4小时",
                    onClick = { openTextDialog(EditDialogType.DIALYSIS_PLAN, s.dialysisPlan) }
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 紧急联系 ===
            ProfileSectionHeader("紧急联系")
            ProfileCard {
                EmergencyContactItem(
                    name = s.emergencyContactName.ifEmpty { "张女士" },
                    relation = s.emergencyContactRelation,
                    phone = s.emergencyContactPhone.ifEmpty { "138-1234-5678" },
                    onEditClick = { openEmergencyContactDialog() },
                    onCallClick = {
                        val phoneNum = s.emergencyContactPhone.ifEmpty { "138-1234-5678" }
                        val cleanPhone = phoneNum.replace(Regex("[^+0-9]"), "")
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$cleanPhone")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "无法打开拨号应用", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(Modifier.height(32.dp))

            // === Save Button ===
            SaveButton(onClick = { saveAll() })
    }

    // ===== Dialogs =====

    when (activeDialog) {
        EditDialogType.NAME, EditDialogType.HEIGHT, EditDialogType.BIRTH_DATE,
        EditDialogType.DRY_WEIGHT, EditDialogType.FIRST_DIALYSIS_DATE,
        EditDialogType.VASCULAR_ACCESS, EditDialogType.HOSPITAL,
        EditDialogType.DIALYSIS_PLAN, EditDialogType.PATIENT_ID -> {
            val title = when (activeDialog) {
                EditDialogType.NAME -> "编辑姓名"
                EditDialogType.HEIGHT -> "编辑身高"
                EditDialogType.BIRTH_DATE -> "编辑出生日期"
                EditDialogType.DRY_WEIGHT -> "编辑干体重"
                EditDialogType.FIRST_DIALYSIS_DATE -> "编辑首次透析日期"
                EditDialogType.VASCULAR_ACCESS -> "编辑血管通路"
                EditDialogType.HOSPITAL -> "编辑透析医院"
                EditDialogType.DIALYSIS_PLAN -> "编辑透析计划"
                EditDialogType.PATIENT_ID -> "编辑患者ID"
                else -> "编辑"
            }
            val hint = when (activeDialog) {
                EditDialogType.HEIGHT -> "请输入身高（cm）"
                EditDialogType.DRY_WEIGHT -> "请输入干体重（kg）"
                EditDialogType.BIRTH_DATE -> "格式: YYYY-MM-DD"
                EditDialogType.FIRST_DIALYSIS_DATE -> "格式: YYYY-MM-DD"
                else -> ""
            }
            TextEditDialog(
                title = title,
                value = editText,
                hint = hint,
                onValueChange = { editText = it },
                onConfirm = { saveTextEdit() },
                onDismiss = { dismissDialog() }
            )
        }
        EditDialogType.GENDER -> {
            GenderEditDialog(
                selected = selectedGender,
                onSelect = { selectedGender = it },
                onConfirm = { saveGender() },
                onDismiss = { dismissDialog() }
            )
        }
        EditDialogType.BLOOD_TYPE -> {
            BloodTypeEditDialog(
                selected = selectedBloodType,
                onSelect = { selectedBloodType = it },
                onConfirm = { saveBloodType() },
                onDismiss = { dismissDialog() }
            )
        }
        EditDialogType.DIALYSIS_TYPE -> {
            DialysisTypeEditDialog(
                selected = selectedDialysisType,
                onSelect = { selectedDialysisType = it },
                onConfirm = { saveDialysisType() },
                onDismiss = { dismissDialog() }
            )
        }
        EditDialogType.EMERGENCY_CONTACT -> {
            EmergencyContactDialog(
                name = ecName,
                phone = ecPhone,
                relation = ecRelation,
                onNameChange = { ecName = it },
                onPhoneChange = { ecPhone = it },
                onRelationChange = { ecRelation = it },
                onConfirm = { saveEmergencyContact() },
                onDismiss = { dismissDialog() }
            )
        }
        EditDialogType.NONE -> { /* no dialog */ }
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
            Canvas(modifier = Modifier.size(20.dp)) {
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
private fun AvatarSection(
    profileName: String,
    patientId: String,
    onAvatarEditClick: () -> Unit
) {
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
                    text = profileName.take(1),
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
                    ) { onAvatarEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(14.dp)) {
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
                        start = Offset(w * 0.62f, h * 0.25f),
                        end = Offset(w * 0.75f, h * 0.38f),
                        strokeWidth = 1.3f * density
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = profileName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = LiquidGlassColors.Foreground,
            letterSpacing = (-0.03).sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "患者ID: $patientId",
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
    highlight: Boolean = false,
    onClick: (() -> Unit)? = null
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
            ) {
                isPressed = true
                onClick?.invoke()
            }
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

        if (onClick != null) {
            Spacer(Modifier.width(4.dp))
            // Chevron
            Text(
                text = "›",
                fontSize = 18.sp,
                color = LiquidGlassColors.Text400
            )
        }
    }
}

@Composable
private fun EmergencyContactItem(
    name: String,
    relation: String,
    phone: String,
    isLast: Boolean = false,
    onEditClick: (() -> Unit)? = null,
    onCallClick: (() -> Unit)? = null
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
            ) {
                isPressed = true
                onEditClick?.invoke()
            }
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
                    ) { onCallClick?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val w = size.width; val h = size.height
                    drawArc(
                        LiquidGlassColors.MedicalGreen,
                        startAngle = 225f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(w * 0.15f, h * 0.15f),
                        size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.7f),
                        style = Stroke(width = 1.5f * density)
                    )
                    drawLine(
                        LiquidGlassColors.MedicalGreen,
                        start = Offset(w * 0.5f, h * 0.68f),
                        end = Offset(w * 0.5f, h * 0.32f),
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

// ===== Dialog Composable Functions =====

@Composable
private fun TextEditDialog(
    title: String,
    value: String,
    hint: String = "",
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = LiquidGlassColors.Foreground
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = hint.ifEmpty { "请输入" },
                            color = LiquidGlassColors.Text500
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LiquidGlassColors.Foreground,
                        unfocusedTextColor = LiquidGlassColors.Foreground,
                        cursorColor = LiquidGlassColors.MedicalCyan,
                        focusedBorderColor = LiquidGlassColors.MedicalCyan,
                        unfocusedBorderColor = LiquidGlassColors.GlassBorder,
                        focusedContainerColor = Color(0xFF2A2A3E),
                        unfocusedContainerColor = Color(0xFF2A2A3E)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认", color = LiquidGlassColors.MedicalCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        }
    )
}

@Composable
private fun GenderEditDialog(
    selected: String,
    onSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf("男", "女")
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "选择性别",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = LiquidGlassColors.Foreground
            )
        },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == option,
                                onClick = { onSelect(option) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = LiquidGlassColors.MedicalCyan,
                                unselectedColor = LiquidGlassColors.Text400
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            color = LiquidGlassColors.Foreground
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认", color = LiquidGlassColors.MedicalCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        }
    )
}

@Composable
private fun BloodTypeEditDialog(
    selected: String,
    onSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "选择血型",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = LiquidGlassColors.Foreground
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState())
                    .height(280.dp)
            ) {
                BLOOD_TYPE_OPTIONS.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == option,
                                onClick = { onSelect(option) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = LiquidGlassColors.MedicalCyan,
                                unselectedColor = LiquidGlassColors.Text400
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            color = LiquidGlassColors.Foreground
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认", color = LiquidGlassColors.MedicalCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        }
    )
}

@Composable
private fun DialysisTypeEditDialog(
    selected: String,
    onSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "选择透析类型",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = LiquidGlassColors.Foreground
            )
        },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                DIALYSIS_TYPE_OPTIONS.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == option,
                                onClick = { onSelect(option) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = LiquidGlassColors.MedicalCyan,
                                unselectedColor = LiquidGlassColors.Text400
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            color = LiquidGlassColors.Foreground
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认", color = LiquidGlassColors.MedicalCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        }
    )
}

@Composable
private fun EmergencyContactDialog(
    name: String,
    phone: String,
    relation: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRelationChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = LiquidGlassColors.Foreground,
        unfocusedTextColor = LiquidGlassColors.Foreground,
        cursorColor = LiquidGlassColors.MedicalCyan,
        focusedBorderColor = LiquidGlassColors.MedicalCyan,
        unfocusedBorderColor = LiquidGlassColors.GlassBorder,
        focusedContainerColor = Color(0xFF2A2A3E),
        unfocusedContainerColor = Color(0xFF2A2A3E)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "编辑紧急联系人",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = LiquidGlassColors.Foreground
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("姓名", color = LiquidGlassColors.Text400) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("电话", color = LiquidGlassColors.Text400) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = relation,
                    onValueChange = onRelationChange,
                    label = { Text("关系", color = LiquidGlassColors.Text400) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认", color = LiquidGlassColors.MedicalCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        }
    )
}