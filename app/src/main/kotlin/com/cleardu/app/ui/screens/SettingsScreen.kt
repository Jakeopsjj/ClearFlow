package com.cleardu.app.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.cleardu.app.data.AppSettings
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.GitHubReleaseChecker
import com.cleardu.app.data.ReleaseInfo
import com.cleardu.app.data.toJson
import com.cleardu.app.BuildConfig
import com.cleardu.app.ui.components.GlassCard
import com.cleardu.app.ui.components.WeatherBackground
import com.cleardu.app.ui.theme.LiquidGlassColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    healthDataManager: HealthDataManager,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // 从全局共享数据层读取 AppSettings，所有设置项同源
    val settings by healthDataManager.settings.collectAsState(initial = null)
    val s = settings ?: return

    // 一键更新设置的工具函数
    fun update(block: (AppSettings) -> AppSettings) {
        scope.launch { healthDataManager.updateSettings(block) }
    }

    val context = LocalContext.current

    // ===== Dialog state variables =====
    var showUnitDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showDialysisPlanDialog by remember { mutableStateOf(false) }
    var showDryWeightDialog by remember { mutableStateOf(false) }
    var dryWeightInput by remember(s.dryWeightTarget) { mutableStateOf(s.dryWeightTarget.removeSuffix(" kg")) }
    var showEmergencyContactDialog by remember { mutableStateOf(false) }
    var emergencyNameInput by remember(s.emergencyContactName) { mutableStateOf(s.emergencyContactName) }
    var emergencyPhoneInput by remember(s.emergencyContactPhone) { mutableStateOf(s.emergencyContactPhone) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showUpdateLogDialog by remember { mutableStateOf(false) }
    var latestRelease by remember { mutableStateOf<ReleaseInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateLogText by remember { mutableStateOf("") }

    // 检查是否需要显示更新日志弹窗
    LaunchedEffect(Unit) {
        val currentVersion = BuildConfig.VERSION_NAME
        if (s.lastSeenVersion != currentVersion) {
            // 首次启动此版本，尝试获取更新日志（按当前构建通道）
            val release = GitHubReleaseChecker.fetchLatestRelease(isDebug = BuildConfig.DEBUG)
            if (release != null) {
                updateLogText = release.body.ifBlank { "版本 ${release.versionName}" }
            } else {
                updateLogText = "版本 $currentVersion\n\n感谢使用清渡，祝您健康每一天。"
            }
            showUpdateLogDialog = true
        }
    }

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
                DarkModeToggleItem(
                    checked = s.darkMode,
                    onCheckedChange = { update { it.copy(darkMode = !it.darkMode) } }
                )
                SettingsNavItem(
                    icon = { NotificationIcon() },
                    iconBg = LiquidGlassColors.TintOrangeBg,
                    iconFg = LiquidGlassColors.MedicalOrange,
                    label = "通知与提醒",
                    sublabel = if (s.strongReminder) "强提醒已开启" else "强提醒已关闭",
                    onClick = onNavigateToNotification
                )
                SettingsNavItem(
                    icon = { UnitIcon() },
                    iconBg = LiquidGlassColors.TintCyanBg,
                    iconFg = LiquidGlassColors.MedicalCyan,
                    label = "单位设置",
                    value = if (s.unitSystem == "kg/mmHg") "kg/mmHg" else "lb/mmHg",
                    onClick = { showUnitDialog = true }
                )
                SettingsNavItem(
                    icon = { LanguageIcon() },
                    iconBg = LiquidGlassColors.TintBlueBg,
                    iconFg = LiquidGlassColors.MedicalBlue,
                    label = "语言",
                    value = if (s.language == "zh_CN") "简体中文" else "English",
                    onClick = { showLanguageDialog = true }
                )
                WeatherBackgroundToggleItem(
                    checked = s.weatherBackgroundEnabled,
                    onCheckedChange = { enabled ->
                        update { it.copy(weatherBackgroundEnabled = enabled) }
                    }
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
                    sublabel = formatBackupTimeShort(s.lastBackupTime),
                    onClick = onNavigateToBackup
                )
                SettingsNavItem(
                    icon = { ExportIcon() },
                    iconBg = LiquidGlassColors.TintIndigoBg,
                    iconFg = LiquidGlassColors.MedicalIndigo,
                    label = "数据导出",
                    value = "PDF/Excel",
                    onClick = { showExportDialog = true }
                )
                SettingsNavItem(
                    icon = { LockIcon() },
                    iconBg = LiquidGlassColors.TintOrangeBg,
                    iconFg = LiquidGlassColors.MedicalOrange,
                    label = "权限管理",
                    onClick = { showPermissionDialog = true }
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
                    sublabel = s.dialysisPlan,
                    onClick = { showDialysisPlanDialog = true }
                )
                SettingsNavItem(
                    icon = { WeightIcon() },
                    iconBg = LiquidGlassColors.TintGreenBg,
                    iconFg = LiquidGlassColors.MedicalGreen,
                    label = "干体重目标",
                    value = s.dryWeightTarget,
                    onClick = { showDryWeightDialog = true }
                )
                ToggleItem(
                    icon = { WaterDropIcon() },
                    iconBg = LiquidGlassColors.TintBlueBg,
                    iconFg = LiquidGlassColors.MedicalBlue,
                    label = "限水提醒",
                    checked = s.waterRestrictionReminder,
                    onCheckedChange = { update { it.copy(waterRestrictionReminder = !it.waterRestrictionReminder) } }
                )
                SettingsNavItem(
                    icon = { EmergencyIcon() },
                    iconBg = LiquidGlassColors.TintRedBg,
                    iconFg = LiquidGlassColors.MedicalRed,
                    label = "紧急联系人",
                    value = "${s.emergencyContactCount}位",
                    onClick = { showEmergencyContactDialog = true }
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
                    value = if (isCheckingUpdate) "检查中..." else BuildConfig.VERSION_NAME,
                    onClick = {
                        if (isCheckingUpdate) return@SettingsNavItem
                        isCheckingUpdate = true
                        scope.launch {
                            // 按当前构建通道（Debug/Release）获取最新版本
                            val release = GitHubReleaseChecker.fetchLatestRelease(isDebug = BuildConfig.DEBUG)
                            latestRelease = release
                            isCheckingUpdate = false
                            if (release != null) {
                                val latestVer = release.versionName
                                // 语义化版本比较：仅当远端版本严格高于当前版本时提示更新
                                if (GitHubReleaseChecker.isNewerVersion(latestVer, BuildConfig.VERSION_NAME)) {
                                    showUpdateDialog = true
                                } else {
                                    Toast.makeText(context, "已是最新版本 ${BuildConfig.VERSION_NAME}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "检查失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                SettingsNavItem(
                    icon = { DocIcon() },
                    iconBg = Color(0x14FFFFFF),
                    iconFg = LiquidGlassColors.Text400,
                    label = "用户协议与隐私政策",
                    onClick = { showPrivacyDialog = true }
                )
                SettingsNavItem(
                    icon = { InfoIcon() },
                    iconBg = Color(0x14FFFFFF),
                    iconFg = LiquidGlassColors.Text400,
                    label = "关于清渡",
                    onClick = { showAboutDialog = true }
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
                    valueColor = LiquidGlassColors.MedicalRed,
                    onClick = { showClearCacheDialog = true }
                )
            }
            SettingsCard {
                SettingsCenterItem(
                    label = "退出登录",
                    labelColor = LiquidGlassColors.MedicalRed,
                    onClick = { showLogoutDialog = true }
                )
            }
        }

        // ===== Dialog invocations =====
        if (showUnitDialog) {
            UnitSettingsDialog(
                current = s.unitSystem,
                onConfirm = { unitSystem ->
                    update { it.copy(unitSystem = unitSystem) }
                    showUnitDialog = false
                },
                onDismiss = { showUnitDialog = false }
            )
        }
        if (showLanguageDialog) {
            LanguageDialog(
                current = s.language,
                onConfirm = { language ->
                    update { it.copy(language = language) }
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false }
            )
        }
        if (showExportDialog) {
            ExportDialog(
                healthDataManager = healthDataManager,
                onDismiss = { showExportDialog = false }
            )
        }
        if (showPermissionDialog) {
            PermissionDialog(
                onDismiss = { showPermissionDialog = false }
            )
        }
        if (showDialysisPlanDialog) {
            DialysisPlanDialog(
                current = s.dialysisPlan,
                onConfirm = { plan ->
                    update { it.copy(dialysisPlan = plan) }
                    showDialysisPlanDialog = false
                },
                onDismiss = { showDialysisPlanDialog = false }
            )
        }
        if (showDryWeightDialog) {
            DryWeightDialog(
                value = dryWeightInput,
                onValueChange = { dryWeightInput = it },
                onConfirm = {
                    val kg = dryWeightInput.toDoubleOrNull()
                    if (kg != null && kg > 0) {
                        update { it.copy(dryWeightTarget = "${dryWeightInput} kg") }
                        showDryWeightDialog = false
                    }
                },
                onDismiss = { showDryWeightDialog = false }
            )
        }
        if (showEmergencyContactDialog) {
            EmergencyContactDialog(
                name = emergencyNameInput,
                phone = emergencyPhoneInput,
                onNameChange = { emergencyNameInput = it },
                onPhoneChange = { emergencyPhoneInput = it },
                onConfirm = {
                    val count = if (emergencyNameInput.isNotBlank() && emergencyPhoneInput.isNotBlank()) 1 else 0
                    update {
                        it.copy(
                            emergencyContactName = emergencyNameInput,
                            emergencyContactPhone = emergencyPhoneInput,
                            emergencyContactCount = count
                        )
                    }
                    showEmergencyContactDialog = false
                    Toast.makeText(context, "紧急联系人已保存", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showEmergencyContactDialog = false }
            )
        }
        if (showPrivacyDialog) {
            PrivacyPolicyDialog(
                onDismiss = { showPrivacyDialog = false }
            )
        }
        if (showAboutDialog) {
            AboutDialog(
                onDismiss = { showAboutDialog = false }
            )
        }
        if (showClearCacheDialog) {
            ClearCacheDialog(
                healthDataManager = healthDataManager,
                onConfirm = {
                    showClearCacheDialog = false
                },
                onDismiss = { showClearCacheDialog = false }
            )
        }
        if (showLogoutDialog) {
            LogoutDialog(
                onConfirm = {
                    showLogoutDialog = false
                    onNavigateToDashboard()
                },
                onDismiss = { showLogoutDialog = false }
            )
        }
        if (showUpdateDialog && latestRelease != null) {
            UpdateAvailableDialog(
                currentVersion = BuildConfig.VERSION_NAME,
                release = latestRelease!!,
                onDownload = {
                    showUpdateDialog = false
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(latestRelease!!.htmlUrl))
                    context.startActivity(intent)
                },
                onDismiss = { showUpdateDialog = false }
            )
        }
        if (showUpdateLogDialog) {
            UpdateLogDialog(
                versionName = BuildConfig.VERSION_NAME,
                logText = updateLogText,
                onDismiss = {
                    showUpdateLogDialog = false
                    // 标记当前版本已查看
                    scope.launch {
                        healthDataManager.updateSettings { it.copy(lastSeenVersion = BuildConfig.VERSION_NAME) }
                    }
                }
            )
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
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
            onCheckedChange = onCheckedChange
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
    valueColor: Color = LiquidGlassColors.Text400,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
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
    labelColor: Color = LiquidGlassColors.Foreground,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
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
private fun DarkModeToggleItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun WeatherBackgroundToggleItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
                .background(LiquidGlassColors.TintCyanBg),
            contentAlignment = Alignment.Center
        ) {
            WeatherIcon()
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "天气背景",
                fontSize = 16.sp,
                color = LiquidGlassColors.Foreground,
                letterSpacing = (-0.01).sp
            )
            Text(
                text = "根据实时天气切换背景",
                fontSize = 12.sp,
                color = LiquidGlassColors.Text400,
                letterSpacing = (-0.01).sp
            )
        }

        IosToggle(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun WeatherIcon() {
    Canvas(modifier = Modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val sunColor = Color(0xFFFFB74D)
        val cloudColor = Color(0xFFE0E0E0)

        // Sun circle
        drawCircle(
            color = sunColor,
            radius = w * 0.2f,
            center = Offset(w * 0.35f, h * 0.35f)
        )

        // Cloud shape (simplified with overlapping circles)
        drawCircle(
            color = cloudColor,
            radius = w * 0.15f,
            center = Offset(w * 0.55f, h * 0.55f)
        )
        drawCircle(
            color = cloudColor,
            radius = w * 0.18f,
            center = Offset(w * 0.7f, h * 0.5f)
        )
        drawCircle(
            color = cloudColor,
            radius = w * 0.15f,
            center = Offset(w * 0.85f, h * 0.58f)
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

// ===== Dialog Composables =====

@Composable
private fun UnitSettingsDialog(
    current: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("单位设置", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == "kg/mmHg", onClick = { selected = "kg/mmHg" })
                    Text("kg/mmHg（公制）", color = LiquidGlassColors.Foreground, modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == "lb/mmHg", onClick = { selected = "lb/mmHg" })
                    Text("lb/mmHg（英制）", color = LiquidGlassColors.Foreground, modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("确定", color = LiquidGlassColors.MedicalBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun LanguageDialog(
    current: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("语言", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == "zh_CN", onClick = { selected = "zh_CN" })
                    Text("简体中文", color = LiquidGlassColors.Foreground, modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == "en_US", onClick = { selected = "en_US" })
                    Text("English", color = LiquidGlassColors.Foreground, modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("确定", color = LiquidGlassColors.MedicalBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ExportDialog(
    healthDataManager: HealthDataManager,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("数据导出", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Text(
                    "支持导出 PDF 格式的健康数据报告。\n\n导出内容包含：\n" +
                    "• 个人资料\n• 健康设置\n• 备份信息\n" +
                    "导出文件将保存至 Downloads/清渡 目录。",
                    color = LiquidGlassColors.Text400,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                if (isExporting) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "正在导出...",
                        color = LiquidGlassColors.MedicalCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isExporting) return@TextButton
                    isExporting = true
                    scope.launch {
                        try {
                            val settings = healthDataManager.settings.first()
                            val exportDir = File(
                                android.os.Environment.getExternalStoragePublicDirectory(
                                    android.os.Environment.DIRECTORY_DOWNLOADS
                                ),
                                "清渡"
                            )
                            exportDir.mkdirs()
                            val fileName = "清渡_健康报告_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.txt"
                            val file = File(exportDir, fileName)
                            file.writeText(buildExportContent(settings))
                            Toast.makeText(context, "已导出到 Downloads/清渡/", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            isExporting = false
                        }
                    }
                },
                enabled = !isExporting
            ) {
                Text("导出到文件", color = LiquidGlassColors.MedicalBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun PermissionDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("权限管理", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold) },
        text = {
            Text(
                "清渡需要以下权限以保证正常运行：\n\n" +
                "• 通知权限 — 发送透析提醒和用药提醒\n" +
                "• 日历权限 — 同步透析日程\n" +
                "• 位置权限 — 紧急联系人定位\n\n" +
                "您可以在系统设置中随时管理这些权限。",
                color = LiquidGlassColors.Text400,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
                onDismiss()
            }) {
                Text("前往系统设置", color = LiquidGlassColors.MedicalBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DialysisPlanDialog(
    current: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(current) }
    val plans = listOf(
        "每周一三五 · 08:00",
        "每周二四六 · 08:00",
        "每周一三五 · 14:00",
        "每周二四六 · 14:00"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("透析计划", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                plans.forEach { plan ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected == plan, onClick = { selected = plan })
                        Text(plan, color = LiquidGlassColors.Foreground, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("确定", color = LiquidGlassColors.MedicalBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DryWeightDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("干体重目标", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Text(
                    "请输入干体重目标值（kg）：",
                    color = LiquidGlassColors.Text400,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("体重 (kg)", color = LiquidGlassColors.Text400) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LiquidGlassColors.Foreground,
                        unfocusedTextColor = LiquidGlassColors.Foreground,
                        focusedBorderColor = LiquidGlassColors.MedicalBlue,
                        unfocusedBorderColor = LiquidGlassColors.Text400.copy(alpha = 0.3f),
                        cursorColor = LiquidGlassColors.MedicalBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定", color = LiquidGlassColors.MedicalBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun EmergencyContactDialog(
    name: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("紧急联系人", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("姓名", color = LiquidGlassColors.Text400) },
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LiquidGlassColors.Foreground,
                        unfocusedTextColor = LiquidGlassColors.Foreground,
                        focusedBorderColor = LiquidGlassColors.MedicalBlue,
                        unfocusedBorderColor = LiquidGlassColors.Text400.copy(alpha = 0.3f),
                        cursorColor = LiquidGlassColors.MedicalBlue
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("电话", color = LiquidGlassColors.Text400) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LiquidGlassColors.Foreground,
                        unfocusedTextColor = LiquidGlassColors.Foreground,
                        focusedBorderColor = LiquidGlassColors.MedicalBlue,
                        unfocusedBorderColor = LiquidGlassColors.Text400.copy(alpha = 0.3f),
                        cursorColor = LiquidGlassColors.MedicalBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("保存", color = LiquidGlassColors.MedicalBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("用户协议与隐私政策", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold) },
        text = {
            Text(
                "感谢您使用清渡。\n\n" +
                "我们重视您的隐私和数据安全。使用本应用即表示您同意以下条款：\n\n" +
                "1. 数据收集：我们仅收集您主动录入的健康数据，包括透析记录、用药记录等。\n\n" +
                "2. 数据使用：您的数据仅用于为您提供健康管理服务，不会用于其他目的。\n\n" +
                "3. 数据安全：所有数据采用加密存储，保障您的隐私安全。\n\n" +
                "4. 数据共享：未经您的明确同意，我们不会将您的数据分享给任何第三方。\n\n" +
                "5. 免责声明：本应用提供的健康管理建议仅供参考，不能替代专业医疗诊断。",
                color = LiquidGlassColors.Text400,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("我已阅读", color = LiquidGlassColors.MedicalBlue)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(LiquidGlassColors.MedicalCyan, LiquidGlassColors.MedicalBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("清", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Text("关于清渡", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column {
                Text(
                    "清渡 — 透析患者健康管理助手",
                    color = LiquidGlassColors.Foreground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "版本：v${BuildConfig.VERSION_NAME}\n" +
                    "构建号：${BuildConfig.VERSION_CODE}\n\n" +
                    "清渡是一款专为透析患者设计的健康管理应用，帮助您轻松记录透析数据、管理用药、设置提醒，让健康管理更简单、更安心。\n\n" +
                    "© 2026 清渡团队",
                    color = LiquidGlassColors.Text400,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = LiquidGlassColors.MedicalBlue)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ClearCacheDialog(
    healthDataManager: HealthDataManager,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("清除缓存", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold) },
        text = {
            Text(
                "确定要清除应用缓存数据吗？\n\n这将清除临时文件和不必要的缓存数据，不会影响您的健康记录和设置。",
                color = LiquidGlassColors.Text400,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    try {
                        // Clear app cache
                        context.cacheDir?.listFiles()?.forEach { it.deleteRecursively() }
                        // Clear external cache
                        context.externalCacheDir?.listFiles()?.forEach { it.deleteRecursively() }
                        Toast.makeText(context, "缓存已清除", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "清除失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    onConfirm()
                }
            }) {
                Text("确定清除", color = LiquidGlassColors.MedicalRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("退出登录", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold) },
        text = {
            Text(
                "确定要退出登录吗？\n\n退出后您将返回首页，但您的健康数据仍会安全保存在本地。",
                color = LiquidGlassColors.Text400,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("退出登录", color = LiquidGlassColors.MedicalRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )}

// ===== Update Dialogs =====

@Composable
private fun UpdateAvailableDialog(
    currentVersion: String,
    release: ReleaseInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("发现新版本", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column {
                Text(
                    "当前版本：$currentVersion\n最新版本：${release.versionName}\n\n" +
                    "更新内容：\n${release.body}",
                    color = LiquidGlassColors.Text400,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDownload) {
                Text("前往下载", color = LiquidGlassColors.MedicalBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后再说", color = LiquidGlassColors.Text400)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun UpdateLogDialog(
    versionName: String,
    logText: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(LiquidGlassColors.MedicalCyan, LiquidGlassColors.MedicalBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("清", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Text("$versionName 更新日志", color = LiquidGlassColors.Foreground, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    logText,
                    color = LiquidGlassColors.Text400,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了", color = LiquidGlassColors.MedicalBlue)
            }
        },
        containerColor = Color(0xFF1C1C2E),
        shape = RoundedCornerShape(16.dp)
    )
}

// ===== Utility Functions =====

/**
 * Format backup timestamp for short display.
 */
private fun formatBackupTimeShort(timestamp: Long): String {
    if (timestamp <= 0L) return "尚未备份"
    val now = System.currentTimeMillis()
    val date = Date(timestamp)
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    val cal = java.util.Calendar.getInstance()
    cal.time = Date(now)
    val todayStart = cal.apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    val tomorrowStart = todayStart + 24 * 60 * 60 * 1000L
    val yesterdayStart = todayStart - 24 * 60 * 60 * 1000L

    return when {
        timestamp >= todayStart && timestamp < tomorrowStart -> "上次备份: 今天 " + timeFmt.format(date)
        timestamp >= yesterdayStart && timestamp < todayStart -> "上次备份: 昨天 " + timeFmt.format(date)
        else -> {
            val dateFmt = SimpleDateFormat("M月d日", Locale.getDefault())
            "上次备份: " + dateFmt.format(date) + " " + timeFmt.format(date)
        }
    }
}

/**
 * Generate export content for data export.
 */
private fun buildExportContent(settings: AppSettings): String {
    val sb = StringBuilder()
    sb.appendLine("=== 清渡 健康数据报告 ===")
    sb.appendLine("导出时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
    sb.appendLine()
    sb.appendLine("--- 个人资料 ---")
    sb.appendLine("姓名: ${settings.profileName}")
    sb.appendLine("性别: ${settings.profileGender}")
    sb.appendLine("出生日期: ${settings.profileBirthDate}")
    sb.appendLine("身高: ${settings.profileHeight}")
    sb.appendLine("血型: ${settings.profileBloodType}")
    sb.appendLine("透析类型: ${settings.profileDialysisType}")
    sb.appendLine("首次透析日期: ${settings.profileFirstDialysisDate}")
    sb.appendLine("血管通路: ${settings.profileVascularAccess}")
    sb.appendLine("患者编号: ${settings.profilePatientId}")
    sb.appendLine()
    sb.appendLine("--- 健康管理 ---")
    sb.appendLine("透析计划: ${settings.dialysisPlan}")
    sb.appendLine("干体重目标: ${settings.dryWeightTarget}")
    sb.appendLine("限水提醒: ${if (settings.waterRestrictionReminder) "开启" else "关闭"}")
    sb.appendLine("单位设置: ${settings.unitSystem}")
    sb.appendLine("语言: ${settings.language}")
    sb.appendLine()
    sb.appendLine("--- 通知设置 ---")
    sb.appendLine("强提醒: ${if (settings.strongReminder) "开启" else "关闭"}")
    sb.appendLine("声音提醒: ${settings.soundReminder}")
    sb.appendLine("震动提醒: ${if (settings.vibrationReminder) "开启" else "关闭"}")
    sb.appendLine("锁屏弹窗: ${if (settings.lockScreenPopup) "开启" else "关闭"}")
    sb.appendLine("提醒时段: ${settings.reminderTimePeriod}")
    sb.appendLine()
    sb.appendLine("--- 备份信息 ---")
    sb.appendLine("自动备份: ${if (settings.autoBackup) "开启" else "关闭"}")
    sb.appendLine("备份频率: ${settings.backupFrequency}")
    sb.appendLine("备份内容: ${settings.backupContent}")
    sb.appendLine("备份加密: ${if (settings.backupEncryption) "是" else "否"}")
    sb.appendLine("上次备份: ${formatBackupTimeShort(settings.lastBackupTime)}")
    sb.appendLine("备份记录数: ${settings.backupHistory.size}")
    sb.appendLine()
    sb.appendLine("--- 紧急联系 ---")
    sb.appendLine("紧急联系人: ${settings.emergencyContactName}")
    sb.appendLine("联系电话: ${settings.emergencyContactPhone}")
    sb.appendLine("关系: ${settings.emergencyContactRelation}")
    sb.appendLine()
    sb.appendLine("--- 报告结束 ---")
    return sb.toString()
}