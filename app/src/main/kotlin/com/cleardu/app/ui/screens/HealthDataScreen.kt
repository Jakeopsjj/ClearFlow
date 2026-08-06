package com.cleardu.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.data.BpData
import com.cleardu.app.data.ElectrolyteData
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.ui.components.FloatingNavigationBar
import com.cleardu.app.ui.components.HealthBpHrCard
import com.cleardu.app.ui.components.HealthElectrolyteGrid
import com.cleardu.app.ui.components.HealthExportButtons
import com.cleardu.app.ui.components.HealthUfTrendCard
import com.cleardu.app.ui.components.HealthWarningBanner
import com.cleardu.app.ui.components.HealthWeightCard
import com.cleardu.app.ui.components.WeatherBackground
import com.cleardu.app.ui.components.SegmentedControl
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 健康数据页面 — "数据" tab。
 *
 * Observes [HealthDataManager] for real-time health data. When a record is
 * saved on the Data Record page, the charts and stats update automatically.
 *
 * 布局复刻参考 HTML：
 *   网格渐变背景 → 可滚动内容 → 标题 → 时间筛选 → 超滤趋势图 →
 *   血压心率卡 → 电解质网格 → 体重记录卡 → 警告横幅 → 导出按钮 →
 *   导航渐隐 → 悬浮导航栏。
 *
 * @param healthDataManager shared data manager for cross-page real-time sync
 * @param onNavItemSelected 导航栏点击回调
 * @param onExport 导出报告回调
 * @param onShare 分享给医生回调
 * @param onWarningClick 警告横幅点击回调
 * @param modifier 外部 modifier
 */
@Composable
fun HealthDataScreen(
    healthDataManager: HealthDataManager,
    onNavItemSelected: (Int) -> Unit = {},
    onExport: () -> Unit = {},
    onShare: () -> Unit = {},
    onWarningClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeFilters = listOf("日", "周", "月", "年")
    var selectedFilter by remember { mutableIntStateOf(1) }
    var selectedNavIndex by remember { mutableIntStateOf(2) } // 数据 tab active

    // === Observe real-time data from shared data manager ===
    val ufTrendData by healthDataManager.ufTrendData.collectAsState(initial = emptyList())
    val bpData by healthDataManager.latestBp.collectAsState(initial = BpData())
    val hrData by healthDataManager.latestHr.collectAsState(initial = 0)
    val electrolyteData by healthDataManager.latestElectrolytes.collectAsState(initial = ElectrolyteData())
    val weightData by healthDataManager.latestWeight.collectAsState(initial = 0.0)
    val weightTrend by healthDataManager.weightTrendData.collectAsState(initial = emptyList())

    // UF target
    val ufTarget = 2000f

    // Check if phosphorus is warning
    val phosphorusWarning = electrolyteData.phosphorus > 1.6

    // Derive average UF
    val ufAverage = if (ufTrendData.isNotEmpty()) {
        (ufTrendData.sum() / ufTrendData.size).toInt()
    } else 0

    // Derive compliance count
    val ufComplianceCount = if (ufTrendData.isNotEmpty()) {
        ufTrendData.count { it <= ufTarget }
    } else 0
    val ufDays = ufTrendData.size

    Box(modifier = Modifier.fillMaxSize()) {
            // === 可滚动内容区 ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = ClearDuDimens.HealthContentTop,
                        start = ClearDuDimens.HealthContentHorizontal,
                        end = ClearDuDimens.HealthContentHorizontal
                    )
            ) {
                // === 页面标题 ===
                Text(
                    text = "健康数据",
                    style = ClearDuTypography.HealthPageTitle,
                    color = LiquidGlassColors.Foreground,
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(ClearDuDimens.HealthTitleBottomMargin))

                // === 时间筛选分段控件 ===
                SegmentedControl(
                    tabs = timeFilters,
                    selectedIndex = selectedFilter,
                    onSelected = { selectedFilter = it }
                )
                Spacer(Modifier.height(ClearDuDimens.HealthFilterBottomMargin))

                // === 超滤量趋势图卡片 ===
                HealthUfTrendCard(
                    data = if (ufTrendData.isNotEmpty()) ufTrendData else listOf(1800f, 2100f, 1950f, 2200f, 1650f, 2050f, 1850f),
                    target = ufTarget,
                    averageValue = ufAverage,
                    complianceDays = ufComplianceCount,
                    totalDays = if (ufDays > 0) ufDays else 7
                )
                Spacer(Modifier.height(ClearDuDimens.HealthCardBottomMargin))

                // === 血压 & 心率卡片 ===
                HealthBpHrCard(
                    systolic = bpData.systolic,
                    diastolic = bpData.diastolic,
                    heartRate = hrData
                )
                Spacer(Modifier.height(ClearDuDimens.HealthCardBottomMargin))

                // === 电解质 2×2 网格 ===
                HealthElectrolyteGrid(
                    potassium = electrolyteData.potassium,
                    phosphorus = electrolyteData.phosphorus,
                    sodium = electrolyteData.sodium,
                    calcium = electrolyteData.calcium
                )
                Spacer(Modifier.height(ClearDuDimens.HealthCardBottomMargin))

                // === 体重记录卡片 ===
                HealthWeightCard(
                    currentWeight = if (weightData > 0) weightData else 65.2,
                    weightTrend = if (weightTrend.isNotEmpty()) weightTrend else listOf(66.1, 65.8, 65.5, 65.9, 65.3, 65.5, 65.2)
                )
                Spacer(Modifier.height(ClearDuDimens.HealthCardBottomMargin))

                // === 警告横幅 ===
                if (phosphorusWarning) {
                    HealthWarningBanner(onClick = onWarningClick)
                    Spacer(Modifier.height(ClearDuDimens.HealthCardBottomMargin))
                }

                // === 导出/分享按钮 ===
                HealthExportButtons(
                    onExport = onExport,
                    onShare = onShare
                )

                // 底部留白
                Spacer(Modifier.height(ClearDuDimens.NavBarHeight + ClearDuDimens.NavBarBottomOffset + 16.dp))
            }

            // === 导航渐隐 ===
            NavBlurFade(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // === 悬浮导航栏 ===
            FloatingNavigationBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = onNavItemSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = ClearDuDimens.NavBarBottomOffset)
            )
    }
}

/**
 * 底部导航渐隐渐变。
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