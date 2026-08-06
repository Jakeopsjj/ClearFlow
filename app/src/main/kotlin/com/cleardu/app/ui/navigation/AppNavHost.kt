package com.cleardu.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.RecordRepository
import com.cleardu.app.ui.screens.BackupScreen
import com.cleardu.app.ui.screens.DashboardScreen
import com.cleardu.app.ui.screens.DataRecordScreen
import com.cleardu.app.ui.screens.HealthDataScreen
import com.cleardu.app.ui.screens.MedicationScreen
import com.cleardu.app.ui.screens.NotificationScreen
import com.cleardu.app.ui.screens.ProfileScreen
import com.cleardu.app.ui.screens.ReminderScreen
import com.cleardu.app.ui.screens.SettingsScreen

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

    // Secondary pages (settings & sub-pages)
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val BACKUP = "backup"
    const val NOTIFICATION = "notification"
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
 * —— 共享数据层 ——
 * [HealthDataManager] 在 NavHost 顶层创建，所有页面共享同一个实例。
 * 记录数据页面保存后，仪表盘和健康数据页面自动实时更新。
 *
 * @param navController 由外部提供时可注入；默认 rememberNavController()
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // === Shared data layer: single source of truth for all screens ===
    val healthDataManager = remember {
        HealthDataManager(RecordRepository(context))
    }

    // 统一的 Tab 导航函数
    val navigateToTab: (Int) -> Unit = remember(navController) {
        { index ->
            val route = NAV_INDEX_TO_ROUTE[index]
            if (route != null) {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
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
            DashboardScreen(
                healthDataManager = healthDataManager,
                onVitalClick = { vitalId ->
                    when (vitalId) {
                        "bp", "hr" -> navigateToTab(1)
                        "weight", "temp" -> navigateToTab(1)
                        else -> navigateToTab(1)
                    }
                },
                onMedRemind = { navigateToTab(3) },
                onQuickAction = { actionId ->
                    when (actionId) {
                        "uf", "bp", "med" -> navigateToTab(1)
                    }
                },
                onNavItemSelected = navigateToTab,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 数据记录 =====
        composable(Routes.DATA_RECORD) {
            DataRecordScreen(
                healthDataManager = healthDataManager,
                onSave = { navController.popBackStack() },
                onNavItemSelected = navigateToTab,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 健康数据 =====
        composable(Routes.HEALTH_DATA) {
            HealthDataScreen(
                healthDataManager = healthDataManager,
                onNavItemSelected = navigateToTab,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 用药管理 =====
        composable(Routes.MEDICATION) {
            MedicationScreen(
                healthDataManager = healthDataManager,
                onNavItemSelected = navigateToTab,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 提醒中心 =====
        composable(Routes.REMINDER) {
            ReminderScreen(
                healthDataManager = healthDataManager,
                onNavItemSelected = navigateToTab,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 设置 =====
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToNotification = { navController.navigate(Routes.NOTIFICATION) },
                onNavigateToBackup = { navController.navigate(Routes.BACKUP) },
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 个人资料 =====
        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 数据备份 =====
        composable(Routes.BACKUP) {
            BackupScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ===== 通知与提醒 =====
        composable(Routes.NOTIFICATION) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}