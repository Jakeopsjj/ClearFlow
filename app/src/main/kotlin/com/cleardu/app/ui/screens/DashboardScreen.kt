package com.cleardu.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.cleardu.app.data.DashboardData
import com.cleardu.app.data.QuickAction
import com.cleardu.app.data.VitalItem
import com.cleardu.app.ui.components.FloatingNavigationBar
import com.cleardu.app.ui.components.FluidBalanceRing
import com.cleardu.app.ui.components.MedicationReminderCard
import com.cleardu.app.ui.components.MeshGradientBackground
import com.cleardu.app.ui.components.QuickActionsRow
import com.cleardu.app.ui.components.VitalCard
import com.cleardu.app.ui.theme.ClearDuDimens
import com.cleardu.app.ui.theme.ClearDuTypography
import com.cleardu.app.ui.theme.LiquidGlassColors

/**
 * Dashboard home screen — the main entry point after onboarding.
 *
 * Layout reproduces the reference HTML 1:1:
 *   Mesh gradient background → scrollable content area →
 *   greeting → fluid ring → vitals grid → med reminder →
 *   quick actions → nav blur fade → floating nav bar.
 *
 * @param data dashboard data model
 * @param onVitalClick callback when a vital card is tapped
 * @param onMedRemind callback when the medication remind button is tapped
 * @param onQuickAction callback when a quick-action button is tapped
 * @param onNavItemSelected callback when a bottom nav item is tapped
 */
@Composable
fun DashboardScreen(
    data: DashboardData,
    onVitalClick: (VitalItem) -> Unit = {},
    onMedRemind: () -> Unit = {},
    onQuickAction: (QuickAction) -> Unit = {},
    onNavItemSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }

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
                        top = ClearDuDimens.DashboardContentTop,
                        start = ClearDuDimens.DashboardContentHorizontal,
                        end = ClearDuDimens.DashboardContentHorizontal
                    )
            ) {
                // === Greeting ===
                GreetingSection(
                    greeting = data.greeting,
                    subtitle = data.greetingSub
                )

                Spacer(Modifier.height(ClearDuDimens.GreetingBottomMargin))

                // === Hero: Fluid Balance Ring ===
                FluidBalanceRing(
                    currentValue = data.fluidIntake,
                    targetValue = data.fluidTarget,
                    statusText = data.fluidStatus,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(ClearDuDimens.RingContainerBottomMargin))

                // === Vitals 2x2 Grid ===
                VitalsGrid(
                    vitals = data.vitals,
                    onVitalClick = onVitalClick
                )

                Spacer(Modifier.height(ClearDuDimens.VitalsGridBottomMargin))

                // === Medication Reminder ===
                MedicationReminderCard(
                    reminder = data.medication,
                    onRemind = onMedRemind,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )

                Spacer(Modifier.height(ClearDuDimens.MedReminderBottomMargin))

                // === Quick Actions ===
                QuickActionsRow(
                    actions = data.quickActions,
                    onAction = onQuickAction
                )

                Spacer(Modifier.height(ClearDuDimens.QuickActionsBottomMargin))

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
 * Top greeting section: title + subtitle.
 */
@Composable
private fun GreetingSection(
    greeting: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = greeting,
            style = ClearDuTypography.GreetingTitle,
            color = LiquidGlassColors.Foreground,
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = ClearDuTypography.GreetingSubtitle,
            color = LiquidGlassColors.Text400,
            textAlign = TextAlign.Start
        )
    }
}

/**
 * 2x2 vitals grid.
 */
@Composable
private fun VitalsGrid(
    vitals: List<VitalItem>,
    onVitalClick: (VitalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ClearDuDimens.VitalsGridGap)
    ) {
        // Group vitals into rows of 2
        vitals.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(ClearDuDimens.VitalsGridGap)
            ) {
                rowItems.forEach { item ->
                    VitalCard(
                        item = item,
                        onClick = { onVitalClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // If odd number of items, fill the remaining space
                if (rowItems.size < 2) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Bottom navigation blur fade gradient.
 *
 * Reproduces `.nav-blur-fade`:
 *   linear-gradient(to top, rgba(0,0,0,0.15) 0%, rgba(0,0,0,0.05) 40%, transparent 70%)
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