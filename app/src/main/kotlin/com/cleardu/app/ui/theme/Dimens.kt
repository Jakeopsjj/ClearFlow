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
    val NavBlurFadeHeight = 88.dp

    // ===== Dashboard: content padding =====
    val DashboardContentTop = 44.dp
    val DashboardContentHorizontal = 20.dp
    val DashboardContentBottom = 72.dp

    // ===== Data record: page layout =====
    val RecordContentTop = 44.dp
    val RecordContentHorizontal = 20.dp
    val RecordContentBottom = 72.dp
    val RecordHeaderBottomMargin = 20.dp
    val RecordSegmentBottomMargin = 20.dp
    val RecordPanelBottomMargin = 16.dp

    // ===== Data record: segmented control =====
    val SegControlRadius = 14.dp
    val SegControlPadding = 3.dp
    val SegIndicatorRadius = 11.dp
    val SegItemPaddingV = 8.dp
    val SegItemPaddingH = 4.dp

    // ===== Data record: ultrafiltration panel =====
    val InputRingSize = 180.dp
    val InputRingStrokeWidth = 10.dp
    val InputRingRadius = 75f
    val InputRingLabelTopMargin = 8.dp
    val InputRingBottomMargin = 16.dp
    val QuickAdjustGap = 8.dp
    val QuickAdjustBtnPaddingH = 14.dp
    val QuickAdjustBtnPaddingV = 8.dp
    val QuickAdjustBottomMargin = 16.dp
    val KeypadGap = 8.dp
    val KeypadKeyHeight = 48.dp
    val KeypadKeyRadius = 16.dp
    val KeypadBottomMargin = 16.dp
    val GoalProgressRadius = 16.dp
    val GoalProgressPaddingH = 14.dp
    val GoalProgressPaddingV = 12.dp
    val GoalProgressBottomMargin = 16.dp
    val GoalBarTrackHeight = 6.dp
    val GoalBarRadius = 3.dp

    // ===== Data record: BP/HR panel =====
    val BpGridGap = 10.dp
    val BpCardRadius = 18.dp
    val BpCardPaddingH = 12.dp
    val BpCardPaddingV = 16.dp
    val BpCardMinHeight = 160.dp
    val BpCardBottomMargin = 12.dp
    val BpAdjustBtnSize = 32.dp
    val BpAdjustGap = 6.dp
    val BpAdjustTopMargin = 10.dp
    val HrCardRadius = 18.dp
    val HrCardPaddingH = 16.dp
    val HrCardPaddingV = 14.dp
    val HrIconSize = 36.dp
    val HrIconRadius = 10.dp
    val HrInfoGap = 12.dp
    val HrAdjustGap = 6.dp

    // ===== Data record: weight/temp panel =====
    val WtGridGap = 10.dp
    val WtCardRadius = 18.dp
    val WtCardPaddingH = 12.dp
    val WtCardPaddingV = 16.dp
    val WtCardMinHeight = 160.dp

    // ===== Data record: elements panel =====
    val ElementsGridGap = 10.dp
    val ElementCardRadius = 18.dp
    val ElementCardPaddingH = 12.dp
    val ElementCardPaddingV = 14.dp
    val ElementCardMinHeight = 140.dp
    val ElementRangeRadius = 6.dp
    val ElementRangePaddingH = 6.dp
    val ElementRangePaddingV = 2.dp
    val ElementRangeTopMargin = 6.dp

    // ===== Data record: medication panel =====
    val MedSearchRadius = 14.dp
    val MedSearchPaddingH = 14.dp
    val MedSearchPaddingV = 10.dp
    val MedSearchGap = 8.dp
    val MedSearchBottomMargin = 12.dp
    val MedItemRadius = 14.dp
    val MedItemPaddingH = 14.dp
    val MedItemPaddingV = 12.dp
    val MedItemGap = 8.dp
    val MedItemIconSize = 32.dp
    val MedItemIconRadius = 9.dp
    val MedItemInfoGap = 10.dp
    val MedDoseBtnPaddingH = 10.dp
    val MedDoseBtnPaddingV = 4.dp
    val MedDoseBtnRadius = 8.dp
    val MedDoseBtnGap = 6.dp

    // ===== Data record: shared components =====
    val ChipsLabelBottomMargin = 8.dp
    val ChipsSectionBottomMargin = 14.dp
    val ChipGap = 8.dp
    val ChipPaddingH = 14.dp
    val ChipPaddingV = 7.dp
    val NoteAreaRadius = 16.dp
    val NoteAreaPaddingH = 14.dp
    val NoteAreaPaddingV = 12.dp
    val NoteAreaMinHeight = 72.dp
    val NoteAreaBottomMargin = 16.dp
    val SaveBtnHeight = 52.dp
    val SaveBtnRadius = 26.dp

    // ===== Health data: page layout =====
    val HealthContentTop = 44.dp
    val HealthContentHorizontal = 20.dp
    val HealthContentBottom = 72.dp
    val HealthTitleBottomMargin = 16.dp
    val HealthFilterBottomMargin = 20.dp
    val HealthCardBottomMargin = 14.dp

    // ===== Health data: chart card =====
    val HealthChartCardRadius = 22.dp
    val HealthChartCardPaddingH = 18.dp
    val HealthChartCardPaddingV = 18.dp
    val HealthChartHeight = 180.dp
    val HealthChartComplianceTopMargin = 10.dp
    val HealthChartAvgValueSize = 20.dp

    // ===== Health data: BP/HR card =====
    val HealthBpHrRadius = 22.dp
    val HealthBpHrPaddingH = 16.dp
    val HealthBpHrPaddingV = 16.dp
    val HealthBpHrGap = 10.dp
    val HealthBpValueSize = 26.dp
    val HealthHrValueSize = 26.dp
    val HealthMiniBarHeight = 40.dp
    val HealthMiniLineHeight = 36.dp

    // ===== Health data: electrolyte grid =====
    val HealthEleGridGap = 10.dp
    val HealthEleCardRadius = 20.dp
    val HealthEleCardPaddingH = 14.dp
    val HealthEleCardPaddingV = 14.dp
    val HealthEleValueSize = 24.dp
    val HealthEleRangeBarHeight = 4.dp
    val HealthEleIndicatorSize = 10.dp

    // ===== Health data: weight card =====
    val HealthWeightRadius = 22.dp
    val HealthWeightPaddingH = 18.dp
    val HealthWeightPaddingV = 18.dp
    val HealthWeightValueSize = 32.dp
    val HealthWeightBarHeight = 6.dp
    val HealthWeightBarRadius = 3.dp
    val HealthMiniWeightHeight = 40.dp

    // ===== Health data: warning banner =====
    val HealthWarningRadius = 18.dp
    val HealthWarningPaddingH = 16.dp
    val HealthWarningPaddingV = 14.dp
    val HealthWarningBtnPaddingH = 14.dp
    val HealthWarningBtnPaddingV = 6.dp
    val HealthWarningBtnRadius = 9999.dp

    // ===== Health data: export buttons =====
    val HealthExportRadius = 16.dp
    val HealthExportPaddingH = 12.dp
    val HealthExportPaddingV = 12.dp
    val HealthExportGap = 10.dp
    val HealthExportIconSize = 18.dp

    // ===== Medication page: layout =====
    val MedPageContentTop = 44.dp
    val MedPageContentHorizontal = 20.dp
    val MedPageContentBottom = 72.dp
    val MedPageTitleBottomMargin = 24.dp

    // ===== Medication page: progress card =====
    val MedProgressCardRadius = 24.dp
    val MedProgressCardPaddingH = 20.dp
    val MedProgressCardPaddingV = 18.dp
    val MedProgressCardGap = 16.dp
    val MedProgressRingSize = 72.dp
    val MedProgressRingStrokeWidth = 7.dp
    val MedProgressRingRadius = 30.dp
    val MedProgressPctFontSize = 15
    val MedProgressTitleBottomMargin = 24.dp

    // ===== Medication page: warning banner =====
    val MedWarningRadius = 16.dp
    val MedWarningPaddingH = 14.dp
    val MedWarningPaddingV = 12.dp
    val MedWarningGap = 10.dp
    val MedWarningIconSize = 28.dp
    val MedWarningIconRadius = 8.dp
    val MedWarningBtnPaddingH = 14.dp
    val MedWarningBtnPaddingV = 6.dp
    val MedWarningBottomMargin = 24.dp

    // ===== Medication page: section label =====
    val MedSectionLabelBottomMargin = 14.dp
    val MedSectionLabelStartPadding = 4.dp

    // ===== Medication page: timeline =====
    val MedTimelineBottomMargin = 20.dp
    val MedTimelineItemGap = 12.dp
    val MedTimelineLineWidth = 1.dp
    val MedTimelineTimeColWidth = 40.dp
    val MedTimelineDotSize = 10.dp
    val MedTimelineLineLeft = 28.dp
    val MedTimelineDotLeft = 36.dp
    val MedTimelineLineOffsetFromTime = -6.dp  // 时间线向左偏移，增加与卡片间距

    // ===== Medication page: medication card =====
    val MedCardRadius = 18.dp
    val MedCardPaddingH = 14.dp
    val MedCardPaddingV = 12.dp
    val MedCardGap = 20.dp  // 时间轴与卡片间距（增大以提升美观度）
    val MedCardIconSize = 38.dp
    val MedCardIconRadius = 10.dp
    val MedCardNameBottomGap = 2.dp
    val MedCardMetaGap = 6.dp
    val MedCardDividerSize = 3.dp

    // ===== Medication page: status indicators =====
    val MedStatusCheckSize = 24.dp
    val MedTakeBtnPaddingH = 16.dp
    val MedTakeBtnPaddingV = 7.dp
    val MedTakenLabelPaddingH = 8.dp
    val MedTakenLabelPaddingV = 3.dp
    val MedOptionalLabelPaddingH = 8.dp
    val MedOptionalLabelPaddingV = 3.dp

    // ===== Medication page: settings entry =====
    val MedSettingsRadius = 18.dp
    val MedSettingsPaddingH = 16.dp
    val MedSettingsPaddingV = 14.dp
    val MedSettingsIconSize = 34.dp
    val MedSettingsIconRadius = 10.dp
    val MedSettingsBottomMargin = 12.dp

    // ===== Medication page: FAB =====
    val MedFabSize = 56.dp
    val MedFabBottomOffset = 82.dp
    val MedFabEndOffset = 24.dp
    val MedFabIconSize = 26.dp

    // ===== Reminder page: layout =====
    val ReminderPageContentTop = 44.dp
    val ReminderPageContentHorizontal = 20.dp
    val ReminderPageTitleBottomMargin = 20.dp

    // ===== Reminder page: countdown card =====
    val ReminderCountdownCardRadius = 24.dp
    val ReminderCountdownCardPadding = 20.dp
    val ReminderCountdownCardBottomMargin = 16.dp
    val ReminderCountdownLabelBottomMargin = 8.dp
    val ReminderCountdownNumberGap = 4.dp
    val ReminderCountdownUnitEndMargin = 6.dp
    val ReminderCountdownDateBottomMargin = 4.dp
    val ReminderCountdownHospitalBottomMargin = 14.dp
    val ReminderNavBtnPaddingH = 14.dp
    val ReminderNavBtnPaddingV = 7.dp
    val ReminderNavBtnIconSize = 14.dp

    // ===== Reminder page: permissions card =====
    val ReminderPermCardRadius = 20.dp
    val ReminderPermCardPadding = 16.dp
    val ReminderPermCardBottomMargin = 16.dp
    val ReminderPermCardTitleBottomMargin = 14.dp
    val ReminderPermItemPaddingV = 10.dp
    val ReminderPermIconSize = 32.dp
    val ReminderPermIconRadius = 9.dp
    val ReminderPermIconInnerSize = 18.dp
    val ReminderPermItemGap = 12.dp

    // ===== Reminder page: section label =====
    val ReminderSectionLabelTopMargin = 4.dp
    val ReminderSectionLabelBottomMargin = 10.dp
    val ReminderSectionLabelStartPadding = 4.dp

    // ===== Reminder page: today reminders =====
    val ReminderTodayCardRadius = 20.dp
    val ReminderTodayCardPadding = 16.dp
    val ReminderTodayCardBottomMargin = 16.dp
    val ReminderTodayItemPaddingV = 10.dp
    val ReminderTodayItemGap = 12.dp
    val ReminderTodayDotSize = 8.dp
    val ReminderTodayTimeWidth = 44.dp
    val ReminderTodayStatusPaddingH = 8.dp
    val ReminderTodayStatusPaddingV = 3.dp

    // ===== Reminder page: settings card =====
    val ReminderSettingsCardRadius = 20.dp
    val ReminderSettingsCardPadding = 16.dp
    val ReminderSettingsCardBottomMargin = 16.dp
    val ReminderSettingsItemPaddingV = 11.dp
    val ReminderSettingsDetailTopGap = 2.dp

    // ===== Reminder page: emergency card =====
    val ReminderEmergencyCardRadius = 20.dp
    val ReminderEmergencyCardPadding = 18.dp
    val ReminderEmergencyCardBottomMargin = 16.dp
    val ReminderEmergencyTitleBottomMargin = 10.dp
    val ReminderEmergencyContactBottomMargin = 4.dp
    val ReminderEmergencyPhoneBottomMargin = 14.dp
    val ReminderEmergencyCallBtnRadius = 16.dp
    val ReminderEmergencyCallBtnPadding = 14.dp
    val ReminderEmergencyFamilyTopMargin = 10.dp

    // ===== Reminder page: toggle =====
    val ToggleWidth = 51.dp
    val ToggleHeight = 31.dp
    val ToggleThumbSize = 27.dp
    val ToggleThumbOffset = 20.dp
    val ToggleThumbPressScale = 1.22f

    // ===== Reminder page: footer note =====
    val ReminderFooterPaddingH = 12.dp
    val ReminderFooterPaddingTop = 8.dp
    val ReminderFooterPaddingBottom = 20.dp

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
