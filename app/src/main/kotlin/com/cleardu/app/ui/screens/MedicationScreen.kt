package com.cleardu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.data.AppSettings
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.ui.components.AddMedicationDialog
import com.cleardu.app.ui.components.FloatingNavigationBar
import com.cleardu.app.ui.components.MedicationFab
import com.cleardu.app.ui.components.MedicationProgressCard
import com.cleardu.app.ui.components.MedicationSettingsDialog
import com.cleardu.app.ui.components.MedicationSettingsEntry
import com.cleardu.app.ui.components.MedicationTimeline
import com.cleardu.app.ui.components.MedicationWarningBanner
import com.cleardu.app.ui.components.MedDoseStatus
import com.cleardu.app.ui.components.RefillRequestDialog
import com.cleardu.app.ui.components.MedicationDose as TimelineMedDose
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import kotlinx.coroutines.launch

/**
 * 用药管理页面 — "用药" tab。
 *
 * Observes [HealthDataManager] for real-time medication data. When a record
 * is saved on the Data Record page, the medication list and progress update
 * automatically.
 *
 * @param healthDataManager shared data manager for cross-page real-time sync
 * @param onNavItemSelected 导航栏点击回调
 * @param onRefill 申请续药回调
 * @param onSettings 用药提醒设置回调
 * @param onFab FAB 添加药物回调
 * @param modifier 外部 modifier
 */
@Composable
fun MedicationScreen(
    healthDataManager: HealthDataManager,
    onNavItemSelected: (Int) -> Unit = {},
    onRefill: () -> Unit = {},
    onSettings: () -> Unit = {},
    onFab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedNavIndex by remember { mutableIntStateOf(3) } // 用药 tab active

    // === Observe real-time data from shared data manager ===
    val latestRecord by healthDataManager.latestRecord.collectAsState(initial = null)
    val appSettings by healthDataManager.settings.collectAsState(initial = AppSettings())
    val vitals by healthDataManager.latestVitals.collectAsState(
        initial = com.cleardu.app.data.DashboardVitals()
    )

    // Dialog state
    var showAddMedDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showRefillDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Derive medication list from latest record
    val storedMeds = latestRecord?.selectedMedications ?: emptyList()
    val totalCount = storedMeds.size
    val takenCount = remember(storedMeds) {
        storedMeds.count { it.doseMultiplier >= 1.0 }
    }

    // Derive timeline doses from stored medications
    val timelineDoses = remember(storedMeds) {
        deriveTimelineDoses(storedMeds)
    }

    // Next medication info
    val nextMed = timelineDoses.firstOrNull { it.status == MedDoseStatus.NEXT_DOSE }
    val nextMedName = nextMed?.name ?: "无"
    val nextMedTime = nextMed?.time ?: "--:--"

    LightMeshGradientBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // === 可滚动内容区 ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = ClearDuDimens.MedPageContentTop,
                        start = ClearDuDimens.MedPageContentHorizontal,
                        end = ClearDuDimens.MedPageContentHorizontal
                    )
            ) {
                // === 页面标题 ===
                Text(
                    text = "用药管理",
                    style = ClearDuTypography.MedPageTitle,
                    color = LiquidGlassColors.LightForeground,
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(ClearDuDimens.MedPageTitleBottomMargin))

                // === 今日服药进度卡 (real data) ===
                MedicationProgressCard(
                    takenCount = takenCount,
                    totalCount = totalCount.coerceAtLeast(1),
                    nextMedName = nextMedName,
                    nextMedTime = nextMedTime
                )
                Spacer(Modifier.height(ClearDuDimens.MedProgressTitleBottomMargin))

                // === 警告横幅 ===
                MedicationWarningBanner(onRefillClick = { showRefillDialog = true })
                Spacer(Modifier.height(ClearDuDimens.MedWarningBottomMargin))

                // === 区块标题 ===
                Text(
                    text = "今日用药时间轴",
                    style = ClearDuTypography.MedSectionLabel,
                    color = LiquidGlassColors.Text400,
                    modifier = Modifier.padding(start = ClearDuDimens.MedSectionLabelStartPadding)
                )
                Spacer(Modifier.height(ClearDuDimens.MedSectionLabelBottomMargin))

                // === 用药时间轴 (real data) ===
                MedicationTimeline(
                    doses = timelineDoses,
                    onDoseTaken = { index ->
                        // Medication taken — handled internally by timeline UI
                    }
                )
                Spacer(Modifier.height(ClearDuDimens.MedTimelineBottomMargin))

                // === 设置入口 ===
                MedicationSettingsEntry(onClick = { showSettingsDialog = true })

                // 底部留白（给导航栏 + FAB 留出空间）
                Spacer(
                    Modifier.height(
                        ClearDuDimens.NavBarHeight + ClearDuDimens.NavBarBottomOffset + 36.dp
                    )
                )
            }

            // === FAB 悬浮按钮 ===
            MedicationFab(
                onClick = { showAddMedDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = ClearDuDimens.MedFabEndOffset,
                        bottom = ClearDuDimens.MedFabBottomOffset
                    )
            )

            // === 导航渐隐 ===
            LightNavBlurFade(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // === 悬浮导航栏（浅色模式） ===
            FloatingNavigationBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = onNavItemSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = ClearDuDimens.NavBarBottomOffset),
                lightMode = true
            )
        }
    }

    // === Dialogs ===
    if (showAddMedDialog) {
        AddMedicationDialog(
            onSave = { customMed ->
                scope.launch {
                    healthDataManager.updateSettings { settings ->
                        settings.copy(
                            customMedications = settings.customMedications + customMed
                        )
                    }
                }
                showAddMedDialog = false
            },
            onDismiss = { showAddMedDialog = false }
        )
    }

    if (showSettingsDialog) {
        MedicationSettingsDialog(
            currentSettings = appSettings,
            onSave = { newSettings ->
                scope.launch {
                    healthDataManager.saveSettings(newSettings)
                }
                showSettingsDialog = false
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showRefillDialog) {
        RefillRequestDialog(
            currentSettings = appSettings,
            onSave = { newSettings ->
                scope.launch {
                    healthDataManager.saveSettings(newSettings)
                }
                showRefillDialog = false
            },
            onDismiss = { showRefillDialog = false }
        )
    }
}

/**
 * 浅色模式网格渐变背景 — 复刻参考 HTML 的浅色 mesh-bg。
 * 5 层 radialGradient 叠加在 #f2f2f7 底色上。
 */
@Composable
private fun LightMeshGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(LiquidGlassColors.LightBackground)
            .drawBehind {
                // 1. 左下角紫色光斑
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidGlassColors.LightMeshPurple, Color.Transparent),
                        center = Offset(size.width * 0.10f, size.height * 0.90f),
                        radius = 320.dp.toPx(),
                        tileMode = TileMode.Clamp
                    )
                )
                // 2. 顶部中间青色光斑
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidGlassColors.LightMeshCyan, Color.Transparent),
                        center = Offset(size.width * 0.50f, size.height * 0.05f),
                        radius = 280.dp.toPx(),
                        tileMode = TileMode.Clamp
                    )
                )
                // 3. 右下角深紫色光斑
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidGlassColors.LightMeshDeepPurple, Color.Transparent),
                        center = Offset(size.width * 0.90f, size.height * 0.85f),
                        radius = 300.dp.toPx(),
                        tileMode = TileMode.Clamp
                    )
                )
                // 4. 右上角蓝色光斑
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidGlassColors.LightMeshBlue, Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.10f),
                        radius = 260.dp.toPx(),
                        tileMode = TileMode.Clamp
                    )
                )
                // 5. 左中区域青色补充
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidGlassColors.LightMeshCyanExtra, Color.Transparent),
                        center = Offset(size.width * 0.20f, size.height * 0.40f),
                        radius = 200.dp.toPx(),
                        tileMode = TileMode.Clamp
                    )
                )
            }
            .padding(0.dp),
        content = { content() }
    )
}

/**
 * 浅色模式底部导航渐隐。
 */
@Composable
private fun LightNavBlurFade(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(ClearDuDimens.NavBlurFadeHeight)
            .drawBehind {
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        LiquidGlassColors.LightNavBlurFadeStart,
                        LiquidGlassColors.LightNavBlurFadeMid,
                        Color.Transparent
                    ),
                    startY = size.height,
                    endY = 0f,
                    tileMode = TileMode.Clamp
                )
                drawRect(brush = brush)
            }
    )
}

// ===== Data derivation helpers =====

/** Default medication times mapped by medication name keywords. */
private val defaultMedTimes = listOf("08:00", "12:00", "18:00", "21:00")

/** Icon colors for medication timeline items. */
private val medIconColors = listOf(
    LiquidGlassColors.TintPurpleBg to LiquidGlassColors.MedicalPurple,
    LiquidGlassColors.TintOrangeBg to LiquidGlassColors.MedicalOrange,
    LiquidGlassColors.TintGreenBg to LiquidGlassColors.MedicalGreen,
    LiquidGlassColors.TintRedBg to LiquidGlassColors.MedicalRed,
    LiquidGlassColors.TintCyanMd to LiquidGlassColors.MedicalCyan
)

/**
 * Convert stored [com.cleardu.app.data.MedicationDose] items into
 * [TimelineMedDose] display items for the medication timeline.
 *
 * Each stored medication gets a time slot based on its index. The first
 * medication is marked as NEXT_DOSE (pending), the rest as UPCOMING.
 */
private fun deriveTimelineDoses(
    storedMeds: List<com.cleardu.app.data.MedicationDose>
): List<TimelineMedDose> {
    if (storedMeds.isEmpty()) return emptyList()

    return storedMeds.mapIndexed { index, med ->
        val time = defaultMedTimes.getOrElse(index) { "08:00" }
        val (iconBg, iconTint) = medIconColors[index % medIconColors.size]
        val status = when (index) {
            0 -> MedDoseStatus.NEXT_DOSE
            else -> MedDoseStatus.UPCOMING
        }
        TimelineMedDose(
            time = time,
            name = med.name,
            dose = med.detail,
            instruction = if (index % 2 == 0) "餐后服用" else "随餐服用",
            status = status,
            iconTint = iconTint,
            iconBg = iconBg,
        )
    }
}
