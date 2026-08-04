package com.cleardu.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.RecordRepository
import com.cleardu.app.ui.screens.HealthDataScreen
import com.cleardu.app.ui.theme.ClearDuTheme

/**
 * 健康数据 Activity — "数据" tab 页面。
 *
 * 展示透析患者的健康数据汇总：
 *  - 超滤量趋势图（7 天折线 + 渐变填充）
 *  - 血压 & 心率双卡片（含迷你柱状图/折线图）
 *  - 电解质 2×2 网格（钾/磷/钠/钙，含范围条和指示器）
 *  - 体重记录卡片（当前体重、目标干体重、进度条）
 *  - 警告横幅（血磷偏高提示）
 *  - 导出报告 / 分享给医生按钮
 *  - 悬浮导航栏（数据 tab active）
 *
 * 导航：
 *  - 导航栏"首页" → MainActivity
 *  - 导航栏"记录" → DataRecordActivity
 */
class HealthDataActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClearDuTheme {
                val healthDataManager = HealthDataManager(RecordRepository(this))
                HealthDataScreen(
                    healthDataManager = healthDataManager,
                    onNavItemSelected = { index ->
                        when (index) {
                            0 -> startMainActivity()
                            1 -> startDataRecordActivity()
                            3 -> startMedicationActivity()
                            4 -> startReminderActivity()
                            // 2 = 数据 (current page, no-op)
                        }
                    },
                    onExport = { /* TODO: 导出健康报告 */ },
                    onShare = { /* TODO: 分享给医生 */ },
                    onWarningClick = { /* TODO: 查看血磷偏高建议 */ },
                    modifier = Modifier.fillMaxSize()
                )
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

    private fun startMedicationActivity() {
        val intent = Intent(this, MedicationActivity::class.java).apply {
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
