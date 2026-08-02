package com.cleardu.app.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.ui.components.BpHrPanel
import com.cleardu.app.ui.components.ElementsPanel
import com.cleardu.app.ui.components.FloatingNavigationBar
import com.cleardu.app.ui.components.MedicationPanel
import com.cleardu.app.ui.components.MeshGradientBackground
import com.cleardu.app.ui.components.NoteTextArea
import com.cleardu.app.ui.components.QuickNoteChips
import com.cleardu.app.ui.components.SaveRecordButton
import com.cleardu.app.ui.components.SegmentedControl
import com.cleardu.app.ui.components.UltrafiltrationPanel
import com.cleardu.app.ui.components.WeightTempPanel
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors
import java.util.Calendar

/**
 * Data record screen — the "记录" page of the app.
 *
 * Layout reproduces the reference HTML:
 *   Mesh gradient background → scrollable content area →
 *   page header (title + date) → segmented control → active tab panel →
 *   quick note chips → note textarea → save button →
 *   nav blur fade → floating nav bar (记录 tab active).
 *
 * No HorizontalPager — tab content is swapped via [when] on [selectedTab].
 * The entire page scrolls vertically.
 *
 * @param onSave callback when the save button is tapped
 * @param onNavItemSelected callback when a bottom nav item is tapped
 * @param modifier outer modifier
 */
@Composable
fun DataRecordScreen(
    onSave: () -> Unit = {},
    onNavItemSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tabs = listOf("超滤量", "血压心率", "体重体温", "元素检测", "用药")
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedNavIndex by remember { mutableIntStateOf(1) } // 记录 tab active

    MeshGradientBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // === Scrollable content area ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = ClearDuDimens.RecordContentTop,
                        start = ClearDuDimens.RecordContentHorizontal,
                        end = ClearDuDimens.RecordContentHorizontal
                    )
            ) {
                // === Page Header ===
                PageHeader()
                Spacer(Modifier.height(ClearDuDimens.RecordHeaderBottomMargin))

                // === Segmented Control ===
                SegmentedControl(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onSelected = { selectedTab = it }
                )
                Spacer(Modifier.height(ClearDuDimens.RecordSegmentBottomMargin))

                // === Active Tab Panel ===
                when (selectedTab) {
                    0 -> UltrafiltrationPanel()
                    1 -> BpHrPanel()
                    2 -> WeightTempPanel()
                    3 -> ElementsPanel()
                    4 -> MedicationPanel()
                }
                Spacer(Modifier.height(ClearDuDimens.RecordPanelBottomMargin))

                // === Shared: Quick Note Chips ===
                QuickNoteChips()

                // === Shared: Note Textarea ===
                NoteTextArea()

                // === Save Button ===
                SaveRecordButton(onClick = onSave)

                // Bottom spacer for nav bar clearance
                Spacer(Modifier.height(ClearDuDimens.NavBarHeight + ClearDuDimens.NavBarBottomOffset + 16.dp))
            }

            // === Navigation blur fade ===
            NavBlurFade(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // === Floating Navigation Bar ===
            // Note: Do NOT update selectedNavIndex here — the parent Activity
            // handles navigation. Updating local state before the transition
            // causes the nav bar to briefly show the wrong selection.
            FloatingNavigationBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = onNavItemSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = ClearDuDimens.NavBarBottomOffset)
            )
        }
    }
}

// ===== Sub-components =====

/**
 * Page header: title "记录数据" + current date subtitle.
 */
@Composable
private fun PageHeader(modifier: Modifier = Modifier) {
    val dateText = remember {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val weekDays = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val weekDay = weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1]
        "${year}年${month}月${day}日 $weekDay"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "记录数据",
            style = ClearDuTypography.RecordPageTitle,
            color = LiquidGlassColors.Foreground,
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = dateText,
            style = ClearDuTypography.RecordPageSubtitle,
            color = LiquidGlassColors.Text400,
            textAlign = TextAlign.Start
        )
    }
}

/**
 * Bottom navigation blur fade gradient.
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
