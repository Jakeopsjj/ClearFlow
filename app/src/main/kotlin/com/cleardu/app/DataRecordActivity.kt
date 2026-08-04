package com.cleardu.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.cleardu.app.ui.screens.DataRecordScreen
import com.cleardu.app.ui.theme.ClearDuTheme

/**
 * Data record Activity — the "记录" page of the app.
 *
 * Displays the data record screen with:
 *  - Segmented tab control (超滤量 / 血压心率 / 体重体温 / 元素检测 / 用药)
 *  - Active tab panel content
 *  - Quick note chips + note textarea
 *  - Save button
 *  - Floating navigation bar (记录 tab active)
 *
 * Navigation:
 *  - Save button → returns to MainActivity (dashboard)
 *  - Nav item "首页" → launches MainActivity and finishes this Activity
 */
class DataRecordActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClearDuTheme {
                DataRecordScreen(
                    onSave = { finish() },
                    onNavItemSelected = { index ->
                        when (index) {
                            0 -> startMainActivity()
                            2 -> startHealthDataActivity()
                            3 -> startMedicationActivity()
                            4 -> startReminderActivity()
                            // 1 = 记录 (current page, no-op)
                        }
                    },
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

    private fun startReminderActivity() {
        val intent = Intent(this, ReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
