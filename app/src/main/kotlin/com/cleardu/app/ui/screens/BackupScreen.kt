package com.cleardu.app.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleardu.app.data.AppSettings
import com.cleardu.app.data.BackupHistoryItem
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.toJson
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
 * 数据备份页面 — 匹配 "数据备份" HTML 参考设计。
 *
 * Sections:
 *  - Backup status card (cloud icon, title, subtitle, progress ring, action buttons)
 *  - 备份设置: auto backup toggle, frequency, wifi-only, backup content, encryption
 *  - 备份历史: history items with date, size, status
 *  - 恢复与导入: restore from backup, import health data, export to file
 *  - Danger zone: 清除所有备份数据
 */
@Composable
fun BackupScreen(
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

    fun update(block: (AppSettings) -> AppSettings) {
        scope.launch { healthDataManager.updateSettings(block) }
    }

    // Dialog states
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showContentDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Track Y position of backup settings section for scrolling
    var scrollableTopY by remember { mutableFloatStateOf(0f) }
    var backupSettingsContentY by remember { mutableFloatStateOf(0f) }

    // === Backup now logic ===
    fun performBackup() {
        scope.launch {
            try {
                val currentSettings = healthDataManager.settings.first()
                val json = currentSettings.toJson().toString()
                val fileName = "backup_${System.currentTimeMillis()}.json"
                val backupDir = File(context.filesDir, "backups")
                backupDir.mkdirs()
                val file = File(backupDir, fileName)
                file.writeText(json)

                val size = "%.1f".format(file.length() / 1024.0 / 1024.0) + " MB"
                val item = BackupHistoryItem(
                    timestamp = System.currentTimeMillis(),
                    fileSize = size,
                    success = true
                )
                update {
                    it.copy(
                        lastBackupTime = System.currentTimeMillis(),
                        backupHistory = listOf(item) + it.backupHistory.take(19)
                    )
                }
                Toast.makeText(context, "备份完成", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "备份失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // === Restore from backup logic ===
    fun performRestore() {
        scope.launch {
            try {
                val backupDir = File(context.filesDir, "backups")
                val backupFiles = backupDir.listFiles { f -> f.isFile && f.extension == "json" }
                    ?.sortedByDescending { it.lastModified() }
                if (backupFiles.isNullOrEmpty()) {
                    Toast.makeText(context, "没有可用的备份文件", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val latestFile = backupFiles.first()
                val json = latestFile.readText()
                val restoredSettings = AppSettings.fromJson(org.json.JSONObject(json))
                healthDataManager.saveSettings(restoredSettings)
                Toast.makeText(context, "恢复完成", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "恢复失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // === Clear all backup data logic ===
    fun performClear() {
        scope.launch {
            try {
                val backupDir = File(context.filesDir, "backups")
                if (backupDir.exists()) {
                    backupDir.listFiles()?.forEach { it.delete() }
                }
                update {
                    it.copy(backupHistory = emptyList(), lastBackupTime = 0L)
                }
                Toast.makeText(context, "备份数据已清除", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "清除失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    WeatherBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .onGloballyPositioned { coordinates ->
                    scrollableTopY = coordinates.localToWindow(Offset.Zero).y
                }
                .padding(start = 20.dp, end = 20.dp, top = 44.dp, bottom = 72.dp)
        ) {
            // === Page Nav ===
            BackupPageNav(
                title = "数据备份",
                backLabel = "设置",
                onBackClick = onNavigateBack
            )

            Spacer(Modifier.height(4.dp))

            // === Backup Status Card ===
            BackupStatusCard(
                lastBackupTime = s.lastBackupTime,
                onBackupNow = { performBackup() },
                onBackupSettings = {
                    scope.launch {
                        scrollState.animateScrollTo(backupSettingsContentY.toInt())
                    }
                }
            )

            Spacer(Modifier.height(28.dp))

            // === Section: 备份设置 ===
            BackupSectionHeader(
                title = "备份设置",
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    backupSettingsContentY = coordinates.localToWindow(Offset.Zero).y - scrollableTopY
                }
            )
            BackupCard {
                BackupToggleItem(
                    label = "自动备份",
                    checked = s.autoBackup,
                    onCheckedChange = { update { it.copy(autoBackup = !it.autoBackup) } }
                )
                BackupNavItem(
                    label = "备份频率",
                    value = s.backupFrequency,
                    onClick = { showFrequencyDialog = true }
                )
                BackupToggleItem(
                    label = "仅Wi-Fi备份",
                    checked = s.backupWifiOnly,
                    onCheckedChange = { update { it.copy(backupWifiOnly = !it.backupWifiOnly) } }
                )
                BackupNavItem(
                    label = "备份内容",
                    value = s.backupContent,
                    onClick = { showContentDialog = true }
                )
                BackupToggleItemWithSub(
                    label = "备份加密",
                    sublabel = "端到端加密保护",
                    checked = s.backupEncryption,
                    onCheckedChange = { update { it.copy(backupEncryption = !it.backupEncryption) } }
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 备份历史 ===
            BackupSectionHeader("备份历史")
            BackupCard {
                if (s.backupHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无备份记录",
                            fontSize = 15.sp,
                            color = LiquidGlassColors.Text400
                        )
                    }
                } else {
                    s.backupHistory.forEach { item ->
                        BackupHistoryItemView(
                            date = formatBackupTime(item.timestamp),
                            fileSize = item.fileSize,
                            success = item.success
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // === Section: 恢复与导入 ===
            BackupSectionHeader("恢复与导入")
            BackupCard {
                BackupNavItem(
                    label = "从备份恢复",
                    value = "选择备份文件",
                    danger = true,
                    onClick = { showRestoreDialog = true }
                )
                BackupNavItem(
                    label = "导入健康数据",
                    value = "从其他App导入",
                    onClick = {
                        Toast.makeText(context, "请在文件管理器中选择要导入的数据文件", Toast.LENGTH_SHORT).show()
                    }
                )
                BackupNavItem(
                    label = "导出为文件",
                    value = "生成Excel/PDF报告",
                    onClick = { showExportDialog = true }
                )
            }

            Spacer(Modifier.height(28.dp))

            // === Danger Zone ===
            BackupCard {
                DangerCenterItem(
                    label = "清除所有备份数据",
                    onClick = { showClearDialog = true }
                )
            }
        }
    }

    // === Dialogs ===
    if (showFrequencyDialog) {
        BackupFrequencyDialog(
            currentValue = s.backupFrequency,
            onSelect = { freq ->
                update { it.copy(backupFrequency = freq) }
                showFrequencyDialog = false
            },
            onDismiss = { showFrequencyDialog = false }
        )
    }

    if (showContentDialog) {
        BackupContentDialog(
            currentValue = s.backupContent,
            onSelect = { content ->
                update { it.copy(backupContent = content) }
                showContentDialog = false
            },
            onDismiss = { showContentDialog = false }
        )
    }

    if (showRestoreDialog) {
        BackupConfirmDialog(
            title = "从备份恢复",
            message = "将使用最新的备份文件恢复数据，当前未保存的数据可能会丢失。确定要继续吗？",
            onConfirm = {
                showRestoreDialog = false
                performRestore()
            },
            onDismiss = { showRestoreDialog = false }
        )
    }

    if (showClearDialog) {
        BackupConfirmDialog(
            title = "清除所有备份数据",
            message = "此操作将删除所有备份文件，且无法恢复。确定要继续吗？",
            onConfirm = {
                showClearDialog = false
                performClear()
            },
            onDismiss = { showClearDialog = false }
        )
    }

    if (showExportDialog) {
        BackupExportDialog(
            onSelect = { format ->
                showExportDialog = false
                scope.launch {
                    try {
                        val exportDir = File(
                            android.os.Environment.getExternalStoragePublicDirectory(
                                android.os.Environment.DIRECTORY_DOWNLOADS
                            ),
                            "清渡"
                        )
                        exportDir.mkdirs()
                        val fileName = if (format == "PDF") {
                            "清渡_健康报告_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
                        } else {
                            "清渡_健康报告_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.xlsx"
                        }
                        val file = File(exportDir, fileName)
                        file.writeText(generateExportContent(s, format))
                        Toast.makeText(context, "文件已导出到 Downloads/清渡/", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showExportDialog = false }
        )
    }
}

// ===== Sub-components =====

@Composable
private fun BackupPageNav(
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
private fun BackupStatusCard(
    lastBackupTime: Long,
    onBackupNow: () -> Unit,
    onBackupSettings: () -> Unit
) {
    var isBackupPressed by remember { mutableStateOf(false) }
    val backupScale by animateFloatAsState(
        targetValue = if (isBackupPressed) 0.97f else 1f,
        animationSpec = tween(200),
        label = "backupBtnScale"
    )

    val lastBackupText = if (lastBackupTime > 0L) {
        "上次备份：" + formatBackupTime(lastBackupTime)
    } else {
        "尚未备份"
    }

    val statusTitle = if (lastBackupTime > 0L) "所有数据已备份" else "建议立即备份"

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        background = LiquidGlassColors.GlassBg,
        border = LiquidGlassColors.GlassBorderSubtle,
        shadowColor = LiquidGlassColors.GlassShadow,
        shadowElevation = 4f,
        specularTop = Color(0x14FFFFFF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cloud icon
            Canvas(modifier = Modifier.size(64.dp)) {
                val w = size.width; val h = size.height
                val cloudPath = Path().apply {
                    moveTo(w * 0.3125f, h * 0.656f)
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(w * 0.3125f, h * 0.5f, w * 0.6875f, h * 0.656f),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(w * 0.6875f, h * 0.656f)
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(w * 0.5f, h * 0.218f, w * 0.8125f, h * 0.5f),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(w * 0.3125f, h * 0.218f, w * 0.5f, h * 0.5f),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 60f,
                        forceMoveTo = false
                    )
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(w * 0.1875f, h * 0.218f, w * 0.375f, h * 0.5f),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    close()
                }
                drawPath(cloudPath, color = LiquidGlassColors.MedicalCyan, style = Stroke(width = 2.5f * density))
                drawPath(cloudPath, color = LiquidGlassColors.MedicalCyan.copy(alpha = 0.1f))

                // Upload arrow
                drawLine(
                    LiquidGlassColors.MedicalCyan,
                    Offset(w * 0.5f, h * 0.375f),
                    Offset(w * 0.5f, h * 0.218f),
                    strokeWidth = 2.5f * density
                )
                drawLine(
                    LiquidGlassColors.MedicalCyan,
                    Offset(w * 0.406f, h * 0.281f),
                    Offset(w * 0.5f, h * 0.218f),
                    strokeWidth = 2.5f * density
                )
                drawLine(
                    LiquidGlassColors.MedicalCyan,
                    Offset(w * 0.594f, h * 0.281f),
                    Offset(w * 0.5f, h * 0.218f),
                    strokeWidth = 2.5f * density
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = statusTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LiquidGlassColors.Foreground,
                letterSpacing = (-0.03).sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = lastBackupText,
                fontSize = 14.sp,
                color = LiquidGlassColors.Text400
            )

            Spacer(Modifier.height(16.dp))

            // Progress ring
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(56.dp)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = size.minDimension * 0.4f,
                        style = Stroke(width = 3f * density)
                    )
                    drawArc(
                        color = if (lastBackupTime > 0L) LiquidGlassColors.MedicalGreen else LiquidGlassColors.MedicalOrange,
                        startAngle = -90f,
                        sweepAngle = if (lastBackupTime > 0L) 360f else 120f,
                        useCenter = false,
                        style = Stroke(width = 3f * density, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
                Text(
                    text = if (lastBackupTime > 0L) "100%" else "--",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (lastBackupTime > 0L) LiquidGlassColors.MedicalGreen else LiquidGlassColors.MedicalOrange
                )
            }

            Spacer(Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary backup button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(backupScale)
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
                            isBackupPressed = true
                            onBackupNow()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "立即备份",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Secondary settings button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LiquidGlassColors.GlassBgLight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBackupSettings() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "备份设置",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = LiquidGlassColors.Foreground
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = LiquidGlassColors.Text400,
        letterSpacing = 0.65.sp,
        modifier = modifier.padding(start = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun BackupCard(
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
private fun BackupToggleItem(
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
        Text(
            text = label,
            fontSize = 16.sp,
            color = LiquidGlassColors.Foreground,
            letterSpacing = (-0.01).sp,
            modifier = Modifier.weight(1f)
        )

        IosToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun BackupToggleItemWithSub(
    label: String,
    sublabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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

        IosToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun BackupNavItem(
    label: String,
    value: String,
    danger: Boolean = false,
    onClick: () -> Unit = {}
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
        Text(
            text = label,
            fontSize = 16.sp,
            color = if (danger) LiquidGlassColors.MedicalRed else LiquidGlassColors.Foreground,
            letterSpacing = (-0.01).sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = 15.sp,
            color = if (danger) LiquidGlassColors.MedicalRed.copy(alpha = 0.7f) else LiquidGlassColors.Text400,
            modifier = Modifier.padding(end = 4.dp)
        )

        Text(
            text = "›",
            fontSize = 18.sp,
            color = if (danger) LiquidGlassColors.MedicalRed.copy(alpha = 0.6f) else LiquidGlassColors.Text400
        )
    }
}

@Composable
private fun BackupHistoryItemView(
    date: String,
    fileSize: String,
    success: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = date,
                fontSize = 16.sp,
                color = LiquidGlassColors.Foreground,
                letterSpacing = (-0.01).sp
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = fileSize,
                fontSize = 13.sp,
                color = LiquidGlassColors.Text400
            )
        }

        // Status badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (success) LiquidGlassColors.TintGreenBg
                    else LiquidGlassColors.TintRedBg
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Canvas(modifier = Modifier.size(12.dp)) {
                val w = size.width; val h = size.height
                if (success) {
                    val path = Path().apply {
                        moveTo(w * 0.2f, h * 0.5f)
                        lineTo(w * 0.45f, h * 0.72f)
                        lineTo(w * 0.8f, h * 0.28f)
                    }
                    drawPath(path, color = LiquidGlassColors.MedicalGreen, style = Stroke(width = 1.8f * density))
                } else {
                    drawLine(
                        LiquidGlassColors.MedicalRed,
                        Offset(w * 0.25f, h * 0.25f),
                        Offset(w * 0.75f, h * 0.75f),
                        strokeWidth = 1.8f * density
                    )
                    drawLine(
                        LiquidGlassColors.MedicalRed,
                        Offset(w * 0.75f, h * 0.25f),
                        Offset(w * 0.25f, h * 0.75f),
                        strokeWidth = 1.8f * density
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (success) "成功" else "失败",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (success) LiquidGlassColors.MedicalGreen else LiquidGlassColors.MedicalRed
            )
        }
    }
}

@Composable
private fun DangerCenterItem(
    label: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = LiquidGlassColors.MedicalRed,
            letterSpacing = (-0.01).sp,
            textAlign = TextAlign.Center
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

// ===== Dialog Composables =====

@Composable
private fun BackupFrequencyDialog(
    currentValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf("每天", "每周", "每月")
    var selected by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = LiquidGlassColors.Foreground,
        textContentColor = LiquidGlassColors.Text400,
        title = {
            Text("备份频率", fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selected = option }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = { selected = option },
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
            TextButton(onClick = { onSelect(selected) }) {
                Text("确定", color = LiquidGlassColors.MedicalCyan)
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
private fun BackupContentDialog(
    currentValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf("全部数据", "仅透析记录", "仅用药记录")
    var selected by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = LiquidGlassColors.Foreground,
        textContentColor = LiquidGlassColors.Text400,
        title = {
            Text("备份内容", fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selected = option }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = { selected = option },
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
            TextButton(onClick = { onSelect(selected) }) {
                Text("确定", color = LiquidGlassColors.MedicalCyan)
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
private fun BackupConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = LiquidGlassColors.Foreground,
        textContentColor = LiquidGlassColors.Text400,
        title = {
            Text(title, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Text(message, fontSize = 15.sp)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "确定",
                    color = if (title.contains("清除")) LiquidGlassColors.MedicalRed else LiquidGlassColors.MedicalCyan
                )
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
private fun BackupExportDialog(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf("PDF", "Excel")
    var selected by remember { mutableStateOf("PDF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = LiquidGlassColors.Foreground,
        textContentColor = LiquidGlassColors.Text400,
        title = {
            Text("选择导出格式", fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selected = option }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == option,
                            onClick = { selected = option },
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
            TextButton(onClick = { onSelect(selected) }) {
                Text("导出", color = LiquidGlassColors.MedicalCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = LiquidGlassColors.Text400)
            }
        }
    )
}

// ===== Utility Functions =====

/**
 * Format a backup timestamp (epoch millis) into a human-readable string.
 * Examples: "今天 08:30", "昨天 08:30", "8月2日 08:30"
 */
private fun formatBackupTime(timestamp: Long): String {
    if (timestamp <= 0L) return "未知"
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
        timestamp >= todayStart && timestamp < tomorrowStart -> "今天 " + timeFmt.format(date)
        timestamp >= yesterdayStart && timestamp < todayStart -> "昨天 " + timeFmt.format(date)
        else -> {
            val dateFmt = SimpleDateFormat("M月d日", Locale.getDefault())
            dateFmt.format(date) + " " + timeFmt.format(date)
        }
    }
}

/**
 * Generate a simple text report for export.
 */
private fun generateExportContent(settings: AppSettings, format: String): String {
    val sb = StringBuilder()
    sb.appendLine("=== 清渡 健康数据报告 ===")
    sb.appendLine("导出时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
    sb.appendLine("格式: $format")
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
    sb.appendLine()
    sb.appendLine("--- 备份信息 ---")
    sb.appendLine("备份频率: ${settings.backupFrequency}")
    sb.appendLine("备份内容: ${settings.backupContent}")
    sb.appendLine("备份加密: ${if (settings.backupEncryption) "是" else "否"}")
    sb.appendLine("上次备份: ${formatBackupTime(settings.lastBackupTime)}")
    sb.appendLine("备份记录数: ${settings.backupHistory.size}")
    sb.appendLine()
    sb.appendLine("--- 报告结束 ---")
    return sb.toString()
}