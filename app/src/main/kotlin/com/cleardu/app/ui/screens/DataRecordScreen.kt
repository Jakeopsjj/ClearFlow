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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cleardu.app.data.HealthDataManager
import com.cleardu.app.data.RecordData
import com.cleardu.app.data.summary
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Data record screen — the "记录" page of the app.
 *
 * Uses the shared [HealthDataManager] so that saved records immediately
 * propagate to the Dashboard and Health Data pages via DataStore flows.
 *
 * @param healthDataManager shared data manager for cross-page real-time sync
 * @param onSave callback when the save button is tapped (after internal save)
 * @param onNavItemSelected callback when a bottom nav item is tapped
 * @param modifier outer modifier
 */
@Composable
fun DataRecordScreen(
    healthDataManager: HealthDataManager,
    onSave: () -> Unit = {},
    onNavItemSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tabs = listOf("超滤量", "血压心率", "体重体温", "元素检测", "用药")
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedNavIndex by remember { mutableIntStateOf(1) } // 记录 tab active

    // === Centralized state ===
    var recordData by remember { mutableStateOf(RecordData()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Helper to update a single field
    fun update(transform: RecordData.() -> RecordData) {
        recordData = recordData.transform()
    }

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
                    0 -> UltrafiltrationPanel(
                        inputValue = recordData.ultrafiltrationMl,
                        goalTarget = recordData.ufGoalTarget,
                        todayRecorded = recordData.ufTodayRecorded,
                        onValueChange = { newVal ->
                            update { copy(ultrafiltrationMl = newVal) }
                        }
                    )
                    1 -> BpHrPanel(
                        systolic = recordData.systolic,
                        diastolic = recordData.diastolic,
                        heartRate = recordData.heartRate,
                        onSystolicChange = { update { copy(systolic = it) } },
                        onDiastolicChange = { update { copy(diastolic = it) } },
                        onHeartRateChange = { update { copy(heartRate = it) } }
                    )
                    2 -> WeightTempPanel(
                        weight = recordData.weight,
                        temperature = recordData.temperature,
                        onWeightChange = { update { copy(weight = it) } },
                        onTemperatureChange = { update { copy(temperature = it) } }
                    )
                    3 -> ElementsPanel(
                        potassium = recordData.potassium,
                        phosphorus = recordData.phosphorus,
                        sodium = recordData.sodium,
                        calcium = recordData.calcium,
                        onPotassiumChange = { update { copy(potassium = it) } },
                        onPhosphorusChange = { update { copy(phosphorus = it) } },
                        onSodiumChange = { update { copy(sodium = it) } },
                        onCalciumChange = { update { copy(calcium = it) } }
                    )
                    4 -> MedicationPanel(
                        medications = recordData.selectedMedications,
                        onMedicationsChange = { update { copy(selectedMedications = it) } }
                    )
                }
                Spacer(Modifier.height(ClearDuDimens.RecordPanelBottomMargin))

                // === Shared: Quick Note Chips ===
                QuickNoteChips(
                    selectedIndex = recordData.quickNoteIndex,
                    onSelected = { update { copy(quickNoteIndex = it) } }
                )

                // === Shared: Note Textarea ===
                NoteTextArea(
                    noteText = recordData.noteText,
                    onNoteChange = { update { copy(noteText = it) } }
                )

                // === Save Button ===
                SaveRecordButton(
                    onClick = {
                        scope.launch {
                            // Persist via shared HealthDataManager
                            val saved = withContext(Dispatchers.IO) {
                                try {
                                    healthDataManager.save(recordData)
                                    true
                                } catch (e: Exception) {
                                    false
                                }
                            }
                            val summary = recordData.summary()
                            if (saved) {
                                snackbarHostState.showSnackbar(
                                    if (summary.isNotEmpty()) "已保存：$summary"
                                    else "记录已保存"
                                )
                            } else {
                                snackbarHostState.showSnackbar("保存失败，请重试")
                            }
                            // Reset form for next entry
                            recordData = RecordData()
                            onSave()
                        }
                    }
                )

                // Bottom spacer for nav bar clearance
                Spacer(Modifier.height(ClearDuDimens.NavBarHeight + ClearDuDimens.NavBarBottomOffset + 16.dp))
            }

            // === Snackbar host ===
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = ClearDuDimens.NavBarHeight + ClearDuDimens.NavBarBottomOffset + 24.dp)
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = LiquidGlassColors.GlassBgStrong,
                    contentColor = LiquidGlassColors.Foreground
                )
            }

            // === Navigation blur fade ===
            NavBlurFade(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )

            // === Floating Navigation Bar ===
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