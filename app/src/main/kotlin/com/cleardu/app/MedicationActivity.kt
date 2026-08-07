package com.cleardu.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.RecordRepository
import com.cleardu.app.data.weather.WeatherBackgroundManager
import com.cleardu.app.ui.components.WeatherBackground
import com.cleardu.app.ui.screens.MedicationScreen
import com.cleardu.app.ui.theme.ClearDuTheme

/**
 * 用药管理 Activity — "用药" tab 页面。
 *
 * 展示透析患者的用药管理信息：
 *  - 今日服药进度环（75% 完成）
 *  - 药物余量警告横幅（磷结合剂剩余 3 天）
 *  - 今日用药时间轴（7 条用药记录，含已服/待服/下次/可选状态）
 *  - 用药提醒设置入口
 *  - 添加药物 FAB
 *  - 悬浮导航栏（用药 tab active）
 *
 * 导航：
 *  - 导航栏"首页" → MainActivity
 *  - 导航栏"记录" → DataRecordActivity
 *  - 导航栏"数据" → HealthDataActivity
 */
class MedicationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val healthDataManager = remember {
                HealthDataManager(RecordRepository(applicationContext))
            }

            // 初始化天气背景管理器
            remember {
                WeatherBackgroundManager.initialize(applicationContext, healthDataManager)
                true
            }

            ClearDuTheme {
                WeatherBackground(modifier = Modifier.fillMaxSize()) {
                    MedicationScreen(
                        healthDataManager = healthDataManager,
                        onNavItemSelected = { index ->
                            when (index) {
                                0 -> startMainActivity()
                                1 -> startDataRecordActivity()
                                2 -> startHealthDataActivity()
                                4 -> startReminderActivity()
                                // 3 = 用药 (current page, no-op)
                            }
                        },
                        onRefill = { /* TODO: 申请续药 */ },
                        onSettings = { /* TODO: 用药提醒设置 */ },
                        onFab = { /* TODO: 添加药物 */ },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun startDataRecordActivity() {
        val intent = Intent(this, DataRecordActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun startHealthDataActivity() {
        val intent = Intent(this, HealthDataActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun startReminderActivity() {
        val intent = Intent(this, ReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
