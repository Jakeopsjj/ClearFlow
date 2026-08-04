package com.cleardu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.cleardu.app.ui.navigation.AppNavHost
import com.cleardu.app.ui.theme.ClearDuTheme

/**
 * 主 Activity — 单 Activity + NavHost 架构入口。
 *
 * 承载 5 个底部导航 Tab 的 NavHost，复用现有 [com.cleardu.app.ui.components.FloatingNavigationBar]。
 * 页面转场由 [AppNavHost] 在 NavHost 根节点统一配置（仅 fadeIn/fadeOut）。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClearDuTheme {
                AppNavHost(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
