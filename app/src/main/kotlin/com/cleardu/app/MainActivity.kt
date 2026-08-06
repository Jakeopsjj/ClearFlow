package com.cleardu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.RecordRepository
import com.cleardu.app.data.weather.WeatherBackgroundManager
import com.cleardu.app.ui.navigation.AppNavHost
import com.cleardu.app.ui.theme.ClearDuTheme

/**
 * 主 Activity — 单 Activity + NavHost 架构入口。
 *
 * 在顶层创建 [HealthDataManager] 单例，同时用于：
 * 1. 主题联动 — 观察 darkMode 设置，传递给 [ClearDuTheme]
 * 2. 全局配置 — 所有页面通过同一个 manager 读写 AppSettings，
 *    保证任意页面修改设置后其他页面立即同步刷新
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 全局共享数据层（单 Activity 作用域）
            val healthDataManager = remember {
                HealthDataManager(RecordRepository(applicationContext))
            }

            // 初始化天气背景管理器（观察 settings 开关，自动启停网络请求）
            remember {
                WeatherBackgroundManager.initialize(applicationContext, healthDataManager)
                true
            }

            // 观察 darkMode 设置，实现实时主题切换
            val settings by healthDataManager.settings.collectAsState(initial = null)
            val darkMode = settings?.darkMode ?: true

            // 观察深色模式设置变化时，使用 key 触发 ClearDuTheme 重组
            androidx.compose.runtime.key(darkMode) {
                ClearDuTheme(darkTheme = darkMode) {
                    AppNavHost(
                        healthDataManager = healthDataManager,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}