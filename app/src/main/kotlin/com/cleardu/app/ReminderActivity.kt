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
import com.cleardu.app.ui.screens.ReminderScreen
import com.cleardu.app.ui.theme.ClearDuTheme

/**
 * 提醒中心 Activity — "提醒" tab 页面。
 *
 * 展示透析患者的提醒管理信息：
 *  - 下次透析倒计时（含呼吸光晕动画）
 *  - 应用权限卡片（6 项权限 + Toggle）
 *  - 今日提醒列表（5 条提醒 + 脉冲圆点）
 *  - 提醒设置（7 项设置 + Toggle）
 *  - 紧急呼叫卡片（红色玻璃 + 呼吸光晕 + 拨打按钮）
 *  - 悬浮导航栏（提醒 tab active）
 *
 * 导航：
 *  - 导航栏"首页" → MainActivity
 *  - 导航栏"记录" → DataRecordActivity
 *  - 导航栏"数据" → HealthDataActivity
 *  - 导航栏"用药" → MedicationActivity
 */
class ReminderActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClearDuTheme {
                val healthDataManager = remember {
                    HealthDataManager(RecordRepository(this@ReminderActivity))
                }
                ReminderScreen(
                    healthDataManager = healthDataManager,
                    onNavItemSelected = { index ->
                        when (index) {
                            0 -> startMainActivity()
                            1 -> startDataRecordActivity()
                            2 -> startHealthDataActivity()
                            3 -> startMedicationActivity()
                            // 4 = 提醒 (current page, no-op)
                        }
                    },
                    onNavigate = { /* TODO: 导航到医院 */ },
                    onCall = { /* TODO: 紧急拨打 */ },
                    onFamilyContact = { /* TODO: 家人联系 */ },
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

    private fun startHealthDataActivity() {
        val intent = Intent(this, HealthDataActivity::class.java).apply {
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
}
