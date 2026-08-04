package com.cleardu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.components.EmergencyCallCard
import com.cleardu.app.ui.components.FloatingNavigationBar
import com.cleardu.app.ui.components.ReminderCountdownCard
import com.cleardu.app.ui.components.ReminderSettingsCard
import com.cleardu.app.ui.components.ReminderTodayList
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * 提醒中心页面 — "提醒" tab。
 *
 * 布局复刻参考 HTML（浅色模式）：
 *   浅色网格渐变背景 → 可滚动内容 → 标题 → 倒计时卡 →
 *   今日提醒 → 提醒设置 → 紧急呼叫 → 底部说明 → 导航渐隐 → 悬浮导航栏。
 *
 * @param onNavItemSelected 导航栏点击回调
 * @param onNavigate 导航到医院回调
 * @param onCall 紧急拨打回调
 * @param onFamilyContact 家人联系回调
 * @param modifier 外部 modifier
 */
@Composable
fun ReminderScreen(
    onNavItemSelected: (Int) -> Unit = {},
    onNavigate: () -> Unit = {},
    onCall: () -> Unit = {},
    onFamilyContact: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedNavIndex by remember { mutableIntStateOf(4) } // 提醒 tab active

    LightMeshGradientBackground(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // === 可滚动内容区 ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = ClearDuDimens.ReminderPageContentTop,
                        start = ClearDuDimens.ReminderPageContentHorizontal,
                        end = ClearDuDimens.ReminderPageContentHorizontal
                    )
            ) {
                // 1. 页面标题
                Text(
                    text = "提醒中心",
                    style = ClearDuTypography.ReminderPageTitle,
                    color = LiquidGlassColors.LightForeground
                )
                Spacer(Modifier.height(ClearDuDimens.ReminderPageTitleBottomMargin))

                // 2. 倒计时英雄卡片
                ReminderCountdownCard(
                    onNavigate = onNavigate,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(ClearDuDimens.ReminderCountdownCardBottomMargin))

                // 3. 章节标题：今日提醒
                SectionLabel(text = "今日提醒")
                Spacer(Modifier.height(ClearDuDimens.ReminderSectionLabelBottomMargin))

                // 4. 今日提醒卡片
                ReminderTodayList(
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(ClearDuDimens.ReminderTodayCardBottomMargin))

                // 5. 章节标题：提醒设置
                SectionLabel(text = "提醒设置")
                Spacer(Modifier.height(ClearDuDimens.ReminderSectionLabelBottomMargin))

                // 6. 提醒设置卡片
                ReminderSettingsCard(
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(ClearDuDimens.ReminderSettingsCardBottomMargin))

                // 7. 紧急呼叫卡片
                EmergencyCallCard(
                    onCall = onCall,
                    onFamilyContact = onFamilyContact,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(ClearDuDimens.ReminderEmergencyCardBottomMargin))

                // 8. 底部说明
                Text(
                    text = "强提醒模式：即使在锁屏状态或App在后台，也会以全屏声音+震动提醒您服药和透析时间",
                    style = ClearDuTypography.ReminderFooterNote,
                    color = LiquidGlassColors.Text400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = ClearDuDimens.ReminderFooterPaddingH,
                            vertical = 0.dp
                        )
                )

                // 底部留白（避开导航栏）
                Spacer(
                    Modifier.height(
                        ClearDuDimens.NavBarHeight +
                                ClearDuDimens.NavBarBottomOffset + 36.dp
                    )
                )
            }

            // === 导航渐隐 ===
            LightNavBlurFade(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // === 悬浮导航栏 ===
            FloatingNavigationBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = { index ->
                    selectedNavIndex = index
                    onNavItemSelected(index)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = ClearDuDimens.NavBarBottomOffset),
                lightMode = true
            )
        }
    }
}

/**
 * 章节标题 — 复刻 .section-header 样式。
 */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = ClearDuTypography.ReminderSectionLabel,
        color = LiquidGlassColors.Text400,
        modifier = Modifier.padding(
            start = ClearDuDimens.ReminderSectionLabelStartPadding
        )
    )
}

/**
 * 浅色模式网格渐变背景 — 复刻参考 HTML 的浅色 mesh-bg。
 * 5 层 radialGradient 叠加在 #f2f2f7 底色上。
 */
@Composable
private fun LightMeshGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(LiquidGlassColors.LightBackground)
            .drawBehind {
                // 1. 左下角紫色光斑
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidGlassColors.LightMeshPurple, Color.Transparent),
                        center = Offset(size.width * 0.10f, size.height * 0.90f),
                        radius = 320.dp.toPx(),
                        tileMode = TileMode.Clamp
                    )
                )
                // 2. 顶部中间青色光斑
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidGlassColors.LightMeshCyan, Color.Transparent),
                        center = Offset(size.width * 0.50f, size.height * 0.05f),
                        radius = 280.dp.toPx(),
                        tileMode = TileMode.Clamp
                    )
                )
                // 3. 右下角深紫色光斑
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidGlassColors.LightMeshDeepPurple, Color.Transparent),
                        center = Offset(size.width * 0.90f, size.height * 0.85f),
                        radius = 300.dp.toPx(),
                        tileMode = TileMode.Clamp
                    )
                )
                // 4. 右上角蓝色光斑
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidGlassColors.LightMeshBlue, Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.10f),
                        radius = 260.dp.toPx(),
                        tileMode = TileMode.Clamp
                    )
                )
                // 5. 左中区域青色补充
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidGlassColors.LightMeshCyanExtra, Color.Transparent),
                        center = Offset(size.width * 0.20f, size.height * 0.40f),
                        radius = 200.dp.toPx(),
                        tileMode = TileMode.Clamp
                    )
                )
            }
            .padding(0.dp),
        content = { content() }
    )
}

/**
 * 浅色模式底部导航渐隐。
 */
@Composable
private fun LightNavBlurFade(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(ClearDuDimens.NavBlurFadeHeight)
            .drawBehind {
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        LiquidGlassColors.LightNavBlurFadeStart,
                        LiquidGlassColors.LightNavBlurFadeMid,
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
