package com.cleardu.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cleardu.app.data.DashboardData
import com.cleardu.app.data.MedicationReminder
import com.cleardu.app.data.QuickAction
import com.cleardu.app.data.VitalItem
import com.cleardu.app.data.VitalStatus
import com.cleardu.app.ui.screens.DashboardScreen
import com.cleardu.app.ui.theme.ClearDuTheme
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Main dashboard Activity — the primary entry point after onboarding.
 *
 * Displays the dashboard home screen with:
 *  - Fluid balance ring
 *  - Vital signs 2x2 grid
 *  - Medication reminder
 *  - Quick action buttons
 *  - Floating navigation bar
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClearDuTheme {
                DashboardRoot(
                    onNavItemSelected = ::handleNavSelection,
                    onQuickAction = ::handleQuickAction
                )
            }
        }
    }

    private fun handleNavSelection(index: Int) {
        when (index) {
            1 -> startActivity(Intent(this, DataRecordActivity::class.java))
            2 -> startActivity(Intent(this, HealthDataActivity::class.java))
            3 -> startActivity(Intent(this, MedicationActivity::class.java))
            // 0 = 首页 (current), 4 = 提醒 (future implementation)
        }
    }

    private fun handleQuickAction(action: QuickAction) {
        when (action.id) {
            "uf", "bp", "med" -> {
                startActivity(Intent(this, DataRecordActivity::class.java))
            }
        }
    }
}

@Composable
private fun DashboardRoot(
    onNavItemSelected: (Int) -> Unit = {},
    onQuickAction: (QuickAction) -> Unit = {}
) {
    val dashboardData = remember {
        DashboardData(
            greeting = "早上好，张先生",
            greetingSub = "今天是您透析后的第 2 天",
            fluidIntake = 1850,
            fluidTarget = 2500,
            fluidStatus = "体液平衡良好",
            vitals = listOf(
                VitalItem(
                    id = "bp",
                    label = "血压",
                    value = "128/82",
                    unit = "mmHg",
                    status = VitalStatus.Normal,
                    accentColor = LiquidGlassColors.MedicalRed
                ),
                VitalItem(
                    id = "hr",
                    label = "心率",
                    value = "72",
                    unit = "bpm",
                    status = VitalStatus.Normal,
                    accentColor = LiquidGlassColors.MedicalRed
                ),
                VitalItem(
                    id = "weight",
                    label = "体重",
                    value = "65.2",
                    unit = "kg",
                    status = VitalStatus.Normal,
                    subValue = "较昨日 -0.3kg",
                    accentColor = LiquidGlassColors.MedicalCyan
                ),
                VitalItem(
                    id = "temp",
                    label = "体温",
                    value = "36.5",
                    unit = "°C",
                    status = VitalStatus.Normal,
                    accentColor = LiquidGlassColors.MedicalOrange
                )
            ),
            medication = MedicationReminder(
                title = "下次用药：降压药",
                detail = "14:00 - 还有 4 小时",
                actionLabel = "提醒我"
            ),
            quickActions = listOf(
                QuickAction(
                    id = "uf",
                    label = "记录超滤",
                    accentColor = LiquidGlassColors.MedicalCyan
                ),
                QuickAction(
                    id = "bp",
                    label = "测血压",
                    accentColor = LiquidGlassColors.MedicalRed
                ),
                QuickAction(
                    id = "med",
                    label = "记用药",
                    accentColor = LiquidGlassColors.MedicalPurple
                ),
                QuickAction(
                    id = "water",
                    label = "喝了水",
                    accentColor = LiquidGlassColors.MedicalCyan
                )
            )
        )
    }

    DashboardScreen(
        data = dashboardData,
        onQuickAction = onQuickAction,
        onNavItemSelected = onNavItemSelected,
        modifier = Modifier.fillMaxSize()
    )
}