package com.cleardu.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.data.DashboardVitals
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.MedicationReminder
import com.cleardu.app.data.QuickAction
import com.cleardu.app.data.VitalItem
import com.cleardu.app.data.VitalStatus
import com.cleardu.app.ui.components.FloatingNavigationBar
import com.cleardu.app.ui.components.FluidBalanceRing
import com.cleardu.app.ui.components.MedicationReminderCard
import com.cleardu.app.ui.components.WeatherBackground
import com.cleardu.app.ui.components.QuickActionsRow
import com.cleardu.app.ui.components.VitalCard
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Dashboard home screen — the main entry point after onboarding.
 *
 * Observes [HealthDataManager] for real-time vitals. When a record is
 * saved on the Data Record page, the dashboard updates automatically.
 *
 * @param healthDataManager shared data manager for cross-page real-time sync
 * @param onVitalClick callback when a vital card is tapped (receives vital id)
 * @param onMedRemind callback when the medication remind button is tapped
 * @param onQuickAction callback when a quick-action button is tapped (receives action id)
 * @param onNavItemSelected callback when a bottom nav item is tapped
 */
@Composable
fun DashboardScreen(
    healthDataManager: HealthDataManager,
    onVitalClick: (String) -> Unit = {},
    onMedRemind: () -> Unit = {},
    onQuickAction: (String) -> Unit = {},
    onNavItemSelected: (Int) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // === Observe real-time vitals from shared data manager ===
    val vitals by healthDataManager.latestVitals.collectAsState(initial = DashboardVitals())

    // Generate time-based greeting
    val greeting = remember { generateTimeBasedGreeting() }
    val greetingSub = remember { generateGreetingSubtitle() }

    // Derive vital items from real-time data
    val vitalItems = remember(vitals) {
        deriveVitalItems(vitals)
    }

    // Derive fluid data
    val fluidIntake = vitals.ultrafiltrationMl
    val fluidTarget = vitals.ufGoalTarget
    val fluidStatus = when {
        fluidIntake <= 0 -> "暂无记录"
        fluidIntake <= fluidTarget -> "体液平衡良好"
        fluidIntake <= fluidTarget * 1.2f -> "体液略偏高"
        else -> "体液偏高，请注意"
    }

    // Medication reminder derived from latest record
    val medicationReminder = remember(vitals) {
        deriveMedicationReminder(vitals)
    }

    val quickActions = listOf(
        QuickAction(id = "uf", label = "记录超滤", accentColor = LiquidGlassColors.MedicalCyan),
        QuickAction(id = "bp", label = "测血压", accentColor = LiquidGlassColors.MedicalRed),
        QuickAction(id = "med", label = "记用药", accentColor = LiquidGlassColors.MedicalPurple),
        QuickAction(id = "water", label = "喝了水", accentColor = LiquidGlassColors.MedicalCyan)
    )

    WeatherBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // === Scrollable content area ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = ClearDuDimens.DashboardContentTop,
                        start = ClearDuDimens.DashboardContentHorizontal,
                        end = ClearDuDimens.DashboardContentHorizontal
                    )
            ) {
                // === Greeting with Settings ===
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    GreetingSection(
                        greeting = greeting,
                        subtitle = greetingSub,
                        modifier = Modifier.weight(1f)
                    )
                    // Settings gear icon
                    SettingsGearButton(onClick = onNavigateToSettings)
                }

                Spacer(Modifier.height(ClearDuDimens.GreetingBottomMargin))

                // === Hero: Fluid Balance Ring ===
                FluidBalanceRing(
                    currentValue = fluidIntake,
                    targetValue = fluidTarget,
                    statusText = fluidStatus,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(ClearDuDimens.RingContainerBottomMargin))

                // === Vitals 2x2 Grid ===
                VitalsGrid(
                    vitals = vitalItems,
                    onVitalClick = onVitalClick
                )

                Spacer(Modifier.height(ClearDuDimens.VitalsGridBottomMargin))

                // === Medication Reminder ===
                MedicationReminderCard(
                    reminder = medicationReminder,
                    onRemind = onMedRemind,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )

                Spacer(Modifier.height(ClearDuDimens.MedReminderBottomMargin))

                // === Quick Actions ===
                QuickActionsRow(
                    actions = quickActions,
                    onAction = { action ->
                        when (action.id) {
                            "water" -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("已记录饮水 200ml")
                                }
                            }
                            else -> onQuickAction(action.id)
                        }
                    }
                )

                Spacer(Modifier.height(ClearDuDimens.QuickActionsBottomMargin))

                // Bottom spacer for nav bar clearance
                Spacer(Modifier.height(ClearDuDimens.NavBarHeight + ClearDuDimens.NavBarBottomOffset + 16.dp))
            }

            // === Snackbar host ===
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = ClearDuDimens.NavBarHeight + ClearDuDimens.NavBarBottomOffset + 24.dp)
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = LiquidGlassColors.GlassBgStrong,
                    contentColor = LiquidGlassColors.Foreground
                )
            }

            // === Navigation blur fade ===
            NavBlurFade(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // === Floating Navigation Bar ===
            FloatingNavigationBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = onNavItemSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = ClearDuDimens.NavBarBottomOffset)
            )
        }
    }
}

// ===== Data derivation helpers =====

/**
 * Derive [VitalItem] list from the latest dashboard vitals.
 */
private fun deriveVitalItems(vitals: DashboardVitals): List<VitalItem> {
    val bpStatus = when {
        vitals.systolic == 0 && vitals.diastolic == 0 -> VitalStatus.Normal
        vitals.systolic > 140 || vitals.diastolic > 90 -> VitalStatus.Warning
        vitals.systolic < 90 || vitals.diastolic < 60 -> VitalStatus.Warning
        else -> VitalStatus.Normal
    }
    val hrStatus = when {
        vitals.heartRate == 0 -> VitalStatus.Normal
        vitals.heartRate > 100 -> VitalStatus.Warning
        vitals.heartRate < 60 -> VitalStatus.Warning
        else -> VitalStatus.Normal
    }

    return listOf(
        VitalItem(
            id = "bp",
            label = "血压",
            value = if (vitals.systolic > 0) "${vitals.systolic}/${vitals.diastolic}" else "--/--",
            unit = "mmHg",
            status = bpStatus,
            accentColor = LiquidGlassColors.MedicalRed
        ),
        VitalItem(
            id = "hr",
            label = "心率",
            value = if (vitals.heartRate > 0) "${vitals.heartRate}" else "--",
            unit = "bpm",
            status = hrStatus,
            accentColor = LiquidGlassColors.MedicalRed
        ),
        VitalItem(
            id = "weight",
            label = "体重",
            value = if (vitals.weight > 0) "${vitals.weight}" else "--",
            unit = "kg",
            status = VitalStatus.Normal,
            accentColor = LiquidGlassColors.MedicalCyan
        ),
        VitalItem(
            id = "temp",
            label = "体温",
            value = if (vitals.temperature > 0) "${vitals.temperature}" else "--",
            unit = "°C",
            status = VitalStatus.Normal,
            accentColor = LiquidGlassColors.MedicalOrange
        )
    )
}

/**
 * Derive medication reminder from the latest record.
 */
private fun deriveMedicationReminder(vitals: DashboardVitals): MedicationReminder {
    if (vitals.medications.isEmpty()) {
        return MedicationReminder(
            title = "今日用药：暂无记录",
            detail = "请前往记录页面添加用药信息",
            actionLabel = "去记录"
        )
    }
    val medNames = vitals.medications.joinToString("、") { it.name }
    return MedicationReminder(
        title = "今日用药：$medNames",
        detail = "共 ${vitals.medications.size} 种药物",
        actionLabel = "提醒我"
    )
}

// ===== Sub-components =====

/**
 * Top greeting section: title + subtitle.
 */
@Composable
private fun GreetingSection(
    greeting: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = greeting,
            style = ClearDuTypography.GreetingTitle,
            color = LiquidGlassColors.Foreground,
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = ClearDuTypography.GreetingSubtitle,
            color = LiquidGlassColors.Text400,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun SettingsGearButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(200),
        label = "gearScale"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(LiquidGlassColors.GlassBgLight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
            val w = size.width; val h = size.height
            val color = LiquidGlassColors.Text400
            val cx = w * 0.5f; val cy = h * 0.5f
            drawCircle(color, radius = w * 0.2f, center = Offset(cx, cy), style = Stroke(width = 1.5f * density))
            drawCircle(color, radius = w * 0.06f, center = Offset(cx, cy))
            // Gear teeth
            for (i in 0 until 8) {
                val angle = (i * 45f) * (Math.PI / 180).toFloat()
                val innerR = w * 0.2f; val outerR = w * 0.28f
                drawLine(
                    color,
                    Offset(cx + innerR * kotlin.math.cos(angle), cy + innerR * kotlin.math.sin(angle)),
                    Offset(cx + outerR * kotlin.math.cos(angle), cy + outerR * kotlin.math.sin(angle)),
                    strokeWidth = 1.5f * density
                )
            }
        }
    }
}

/**
 * 2x2 vitals grid.
 */
@Composable
private fun VitalsGrid(
    vitals: List<VitalItem>,
    onVitalClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ClearDuDimens.VitalsGridGap)
    ) {
        vitals.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.VitalsGridGap)
            ) {
                rowItems.forEach { item ->
                    VitalCard(
                        item = item,
                        onClick = { onVitalClick(item.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size < 2) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Bottom navigation blur fade gradient.
 */
@Composable
private fun NavBlurFade(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(ClearDuDimens.NavBlurFadeHeight)
            .drawBehind {
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        LiquidGlassColors.NavBlurFadeStart,
                        LiquidGlassColors.NavBlurFadeMid,
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

// ===== Helper functions =====

/**
 * Generate a time-based greeting based on the current hour.
 */
private fun generateTimeBasedGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 5..11 -> "早上好"
        hour in 12..17 -> "下午好"
        else -> "晚上好"
    }
}

/**
 * Generate a contextual subtitle with date.
 */
private fun generateGreetingSubtitle(): String {
    val cal = Calendar.getInstance()
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val weekDays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
    val weekday = weekDays[dayOfWeek - 1]
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val dayDiff = cal.get(Calendar.DAY_OF_YEAR) % 3 + 1
    return "${month}月${day}日 $weekday · 透析后第 ${dayDiff} 天"
}