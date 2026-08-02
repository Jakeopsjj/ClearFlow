package com.cleardu.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Layout / dimension tokens ported from the HTML design CSS.
 *
 * All values are in dp so the layout adapts across Android screen densities
 * while matching the reference design 1:1 at xxhdpi (≈ iOS @3x).
 */
object ClearDuDimens {

    // ===== Screen padding =====
    val ScreenHorizontalPadding = 24.dp
    val ScreenTopPadding = 44.dp
    val ScreenBottomScrollPadding = 140.dp

    // ===== Welcome section =====
    val WelcomeTopPadding = 40.dp
    val WelcomeLogoBottomMargin = 20.dp
    val WelcomeTitleBottomMargin = 8.dp
    val WelcomeSubtitleBottomMargin = 12.dp
    val WelcomeDescMaxWidth = 280.dp

    val LogoSize = 80.dp
    val LogoRingBorderWidth = 2.dp

    // ===== Permission list =====
    val PermissionListGap = 12.dp
    val PermissionListBottomMargin = 28.dp

    val PermissionCardPaddingHorizontal = 16.dp
    val PermissionCardPaddingVertical = 14.dp
    val PermissionCardGap = 14.dp
    val PermissionCardRadius = 18.dp

    val PermIconSize = 44.dp
    val PermIconStrokeWidth = 22.dp // SVG icon size, drawn inside [PermIconSize]

    val PermButtonPaddingHorizontal = 16.dp
    val PermButtonPaddingHorizontalGranted = 12.dp
    val PermButtonPaddingVertical = 6.dp
    val PermButtonGap = 4.dp

    val CriticalBadgePaddingHorizontal = 6.dp
    val CriticalBadgePaddingVertical = 2.dp
    val CriticalBadgeRadius = 4.dp
    val CriticalBadgeOffsetTop = 8.dp
    val CriticalBadgeOffsetEnd = 12.dp

    // ===== Bottom section =====
    val BottomSectionGap = 12.dp
    val BottomSectionBottomPadding = 10.dp

    val StartButtonHeight = 56.dp
    val StartButtonMaxWidth = 280.dp
    val StartButtonRadius = 28.dp

    // ===== Glass material =====
    val GlassElevation = 0.dp // we draw shadows manually for multi-layer effect
    val GlassBorderWidth = 1.dp
    val GlassSpecularHeightFraction = 0.5f // top 50% specular highlight

    // ===== Dashboard: greeting =====
    val GreetingBottomMargin = 20.dp

    // ===== Dashboard: fluid balance ring =====
    val RingSize = 200.dp
    val RingStrokeWidth = 14.dp
    val RingRadius = 86.dp
    val RingCenterGap = 6.dp
    val RingStatusTopMargin = 12.dp
    val RingContainerBottomMargin = 24.dp

    // ===== Dashboard: vitals grid =====
    val VitalsGridGap = 12.dp
    val VitalsGridBottomMargin = 16.dp
    val VitalCardPadding = 14.dp
    val VitalCardRadius = 20.dp
    val VitalIconSize = 28.dp
    val VitalIconRadius = 8.dp
    val VitalHeaderBottomMargin = 10.dp
    val VitalSubTopMargin = 4.dp
    val VitalStatusTagPaddingH = 8.dp
    val VitalStatusTagPaddingV = 2.dp

    // ===== Dashboard: medication reminder =====
    val MedReminderPaddingH = 16.dp
    val MedReminderPaddingV = 14.dp
    val MedReminderRadius = 20.dp
    val MedReminderBottomMargin = 20.dp
    val MedIconSize = 36.dp
    val MedIconRadius = 10.dp
    val MedInfoGap = 12.dp
    val MedTitleDetailGap = 2.dp
    val RemindBtnPaddingH = 14.dp
    val RemindBtnPaddingV = 6.dp

    // ===== Dashboard: quick actions =====
    val QuickActionsPaddingH = 4.dp
    val QuickActionsBottomMargin = 16.dp
    val QuickActionBtnSize = 48.dp
    val QuickActionGap = 6.dp

    // ===== Dashboard: navigation bar =====
    val NavBarWidth = 280.dp
    val NavBarHeight = 56.dp
    val NavBarRadius = 28.dp
    val NavBarBottomOffset = 12.dp
    val NavBarPaddingH = 8.dp
    val NavItemSize = 48.dp
    val NavIconSize = 24.dp
    val NavIndicatorWidth = 18.dp
    val NavIndicatorHeight = 4.dp
    val NavIndicatorRadius = 2.dp
    val NavIndicatorBottom = 4.dp
    val NavBlurFadeHeight = 76.dp

    // ===== Dashboard: content padding =====
    val DashboardContentTop = 44.dp
    val DashboardContentHorizontal = 20.dp
    val DashboardContentBottom = 72.dp

    // ===== Mesh background glow radii (px-equivalent at design scale) =====
    object Mesh {
        val PurpleRadiusX = 320.dp
        val PurpleRadiusY = 300.dp
        val CyanRadiusX = 280.dp
        val CyanRadiusY = 260.dp
        val DeepPurpleRadiusX = 300.dp
        val DeepPurpleRadiusY = 280.dp
        val BlueRadiusX = 260.dp
        val BlueRadiusY = 240.dp
        val GreenRadius = 200.dp
    }
}

/**
 * Easing curves and motion durations ported from the CSS cubic-beziers
 * used by the reference design.
 *
 * `cubic-bezier(0.32, 0.72, 0, 1)` is the iOS standard ease used by Apple's
 * liquid-glass interactive cards; it produces a snappy in-out motion.
 */
object ClearDuMotion {
    const val EaseIOSTension = 320f
    const val EaseIOSFriction = 72f

    const val CardTapDurationMs = 200
    const val ButtonTapDurationMs = 150
    const val ButtonPressScale = 0.96f
    const val CardPressScale = 0.98f
    const val PermissionButtonPressScale = 0.92f

    const val LogoPulseDurationMs = 3000
    const val ButtonGlowDurationMs = 2000

    const val PermissionGrantPulseMs = 200
    const val PermissionGrantScale = 0.98f
}
