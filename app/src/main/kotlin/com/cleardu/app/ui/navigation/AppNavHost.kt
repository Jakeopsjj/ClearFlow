package com.cleardu.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cleardu.app.data.DashboardData
import com.cleardu.app.data.MedicationReminder
import com.cleardu.app.data.QuickAction
import com.cleardu.app.data.VitalItem
import com.cleardu.app.data.VitalStatus
import com.cleardu.app.ui.screens.DashboardScreen
import com.cleardu.app.ui.screens.DataRecordScreen
import com.cleardu.app.ui.screens.HealthDataScreen
import com.cleardu.app.ui.screens.MedicationScreen
import com.cleardu.app.ui.screens.ReminderScreen
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 全局路由常量。
 *
 * 5 个底部导航 Tab 各对应一条路由，不携带参数。
 */
object Routes {
    const val DASHBOARD = "dashboard"
    const val DATA_RECORD = "data_record"
    const val HEALTH_DATA = "health_data"
    const val MEDICATION = "medication"
    const val REMINDER = "reminder"
}

/** 导航栏索引 → 路由映射 */
private val NAV_INDEX_TO_ROUTE = mapOf(
    0 to Routes.DASHBOARD,
    1 to Routes.DATA_RECORD,
    2 to Routes.HEALTH_DATA,
    3 to Routes.MEDICATION,
    4 to Routes.REMINDER
)

/** 淡入淡出动画时长（ms） */
private const val FADE_DURATION = 300

/**
 * 应用根 NavHost。
 *
 * —— 全局转场配置 ——
 * 仅使用 fadeIn / fadeOut 透明度插值，禁止 slide 位移类动画。
 * enterTransition / exitTransition / popEnterTransition / popExitTransition
 * 全部在 NavHost 根节点统一声明，各 composable destination 不重写。
 *
 * —— 导航跳转策略 ——
 * launchSingleTop  — 目标页已在栈顶时不创建新实例
 * popUpTo(start)   — 弹出至起始目的地，保持返回栈整洁
 * saveState        — 弹出时保存页面状态
 * restoreState     — 导航时恢复目标页面保存的状态
 *
 * @param navController 由外部提供时可注入；默认 rememberNavController()
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    // 统一的 Tab 导航函数
    val navigateToTab: (Int) -> Unit = remember(navController) {
        { index ->
            val route = NAV_INDEX_TO_ROUTE[index]
            if (route != null) {
                navController.navigate(route) {
                    // 弹出至起始目的地，保存当前页状态
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    // 单顶模式：目标已在栈顶则不新建
                    launchSingleTop = true
                    // 恢复目标页保存的状态
                    restoreState = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = modifier,
        // === 全局淡入淡出转场（禁止 slide） ===
        enterTransition = { fadeIn(animationSpec = tween(FADE_DURATION)) },
        exitTransition = { fadeOut(animationSpec = tween(FADE_DURATION)) },
        popEnterTransition = { fadeIn(animationSpec = tween(FADE_DURATION)) },
        popExitTransition = { fadeOut(animationSpec = tween(FADE_DURATION)) }
    ) {
        // ===== 首页 =====
        composable(Routes.DASHBOARD) {
            val dashboardData = remember { createDashboardData() }
            DashboardScreen(
                data = dashboardData,
                onQuickAction = { action ->
                    when (action.id) {
                        "uf", "bp", "med" -> navigateToTab(1)
                    }
                },
                onNavItemSelected = navigateToTab,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 数据记录 =====
        composable(Routes.DATA_RECORD) {
            DataRecordScreen(
                onSave = { navController.popBackStack() },
                onNavItemSelected = navigateToTab,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 健康数据 =====
        composable(Routes.HEALTH_DATA) {
            HealthDataScreen(
                onNavItemSelected = navigateToTab,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 用药管理 =====
        composable(Routes.MEDICATION) {
            MedicationScreen(
                onNavItemSelected = navigateToTab,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 提醒中心 =====
        composable(Routes.REMINDER) {
            ReminderScreen(
                onNavItemSelected = navigateToTab,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// region Dashboard 模拟数据

private fun createDashboardData(): DashboardData {
    return DashboardData(
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

// endregion
