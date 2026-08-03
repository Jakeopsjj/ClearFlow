package com.cleardu.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Typography tokens ported from the HTML design.
 *
 * Letter-spacing values are negative (em) to mirror the Apple-system tight
 * tracking used in the reference. Sizes are in sp to honor user font-scale
 * preferences while still matching the design px-for-px at the default scale.
 */
object ClearDuTypography {
    val WelcomeTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W700,
        fontSize = 34.sp,
        lineHeight = 41.sp, // 1.2x
        letterSpacing = (-0.03).sp
    )

    val WelcomeSubtitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 17.sp,
        letterSpacing = (-0.01).sp
    )

    val WelcomeDesc = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 22.sp, // 1.6x
        letterSpacing = (-0.01).sp
    )

    val PermissionName = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.01).sp
    )

    val PermissionDesc = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 17.sp, // 1.4x
        letterSpacing = (-0.01).sp
    )

    val PermissionButton = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 13.sp
    )

    val Agreement = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.01).sp
    )

    val StartButton = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W700,
        fontSize = 18.sp,
        letterSpacing = (-0.01).sp
    )

    val CriticalBadge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 10.sp,
        letterSpacing = (-0.01).sp
    )

    // ===== Dashboard: greeting =====
    val GreetingTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.03).sp
    )

    val GreetingSubtitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        letterSpacing = (-0.01).sp
    )

    // ===== Dashboard: ring =====
    val RingValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 42.sp,
        letterSpacing = (-0.04).sp,
        lineHeight = 42.sp
    )

    val RingValueUnit = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 18.sp
    )

    val RingLabel = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        letterSpacing = (-0.01).sp
    )

    val RingStatus = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    )

    // ===== Dashboard: vital cards =====
    val VitalValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 26.sp,
        letterSpacing = (-0.03).sp,
        lineHeight = 29.sp
    )

    val VitalUnit = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp
    )

    val VitalStatusTag = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 11.sp,
        letterSpacing = (-0.01).sp
    )

    val VitalSub = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 11.sp
    )

    // ===== Dashboard: medication reminder =====
    val MedTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp
    )

    val MedDetail = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )

    val RemindButton = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 13.sp
    )

    // ===== Dashboard: quick actions =====
    val QuickActionLabel = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 11.sp,
        letterSpacing = (-0.01).sp
    )

    // ===== Data record: page header =====
    val RecordPageTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.03).sp
    )

    val RecordPageSubtitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        letterSpacing = (-0.01).sp
    )

    // ===== Data record: segmented control =====
    val SegItem = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp,
        letterSpacing = (-0.01).sp
    )

    // ===== Data record: ultrafiltration panel =====
    val InputDisplay = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 48.sp,
        letterSpacing = (-0.04).sp,
        lineHeight = 48.sp
    )

    val InputUnit = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 18.sp
    )

    val InputLabel = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        letterSpacing = (-0.01).sp
    )

    val KeyText = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W500,
        fontSize = 22.sp
    )

    val KeyActionText = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp
    )

    val QuickAdjustText = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W600,
        fontSize = 13.sp,
        letterSpacing = (-0.02).sp
    )

    val GoalLabel = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )

    val GoalValues = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )

    // ===== Data record: BP/HR panel =====
    val BpCardLabel = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )

    val BpCardValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 36.sp,
        letterSpacing = (-0.03).sp,
        lineHeight = 36.sp
    )

    val BpCardUnit = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )

    val HrInfoValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 30.sp,
        letterSpacing = (-0.03).sp,
        lineHeight = 33.sp
    )

    // ===== Data record: weight/temp panel =====
    val WtCardValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 34.sp,
        letterSpacing = (-0.03).sp,
        lineHeight = 34.sp
    )

    // ===== Data record: elements panel =====
    val ElementSymbol = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 20.sp
    )

    val ElementName = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 11.sp
    )

    val ElementValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp,
        letterSpacing = (-0.02).sp,
        lineHeight = 28.sp
    )

    val ElementUnit = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 11.sp
    )

    val ElementRange = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 10.sp
    )

    // ===== Data record: medication panel =====
    val MedSearchPlaceholder = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    )

    val MedListTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )

    val MedItemName = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp
    )

    val MedDoseBtn = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp
    )

    // ===== Data record: shared components =====
    val ChipText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 13.sp
    )

    val NoteText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )

    val SaveBtnText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 17.sp,
        letterSpacing = (-0.01).sp
    )

    // ===== Health data: page =====
    val HealthPageTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W700,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.03).sp
    )

    val HealthCardTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 16.sp,
        letterSpacing = (-0.01).sp
    )

    val HealthCardSubtitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        letterSpacing = (-0.01).sp
    )

    val HealthAvgValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 20.sp,
        letterSpacing = (-0.02).sp
    )

    val HealthAvgLabel = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )

    val HealthComplianceText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 13.sp,
        letterSpacing = (-0.01).sp
    )

    val HealthVitalLabel = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        letterSpacing = (-0.01).sp
    )

    val HealthBpValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 26.sp,
        letterSpacing = (-0.03).sp,
        lineHeight = 29.sp
    )

    val HealthHrValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 26.sp,
        letterSpacing = (-0.03).sp,
        lineHeight = 29.sp
    )

    val HealthVitalUnit = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )

    val HealthStatusTag = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 11.sp,
        letterSpacing = (-0.01).sp
    )

    val HealthEleSymbol = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 18.sp
    )

    val HealthEleName = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 11.sp
    )

    val HealthEleValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 24.sp,
        letterSpacing = (-0.02).sp,
        lineHeight = 26.sp
    )

    val HealthEleUnit = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 11.sp
    )

    val HealthEleRange = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W400,
        fontSize = 10.sp
    )

    val HealthWeightValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W700,
        fontSize = 32.sp,
        letterSpacing = (-0.03).sp,
        lineHeight = 34.sp
    )

    val HealthWeightLabel = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp
    )

    val HealthWeightScale = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W400,
        fontSize = 10.sp
    )

    val HealthWarningText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 13.sp,
        lineHeight = 19.sp
    )

    val HealthWarningBold = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W700,
        fontSize = 13.sp
    )

    val HealthWarningBtn = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 12.sp
    )

    val HealthExportBtn = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        letterSpacing = (-0.01).sp
    )

    val HealthExportIcon = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W400,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        lineHeight = 12.sp
    )

    val HealthChartAxis = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.W400,
        fontSize = 10.sp
    )

    val HealthChartTarget = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.W500,
        fontSize = 10.sp
    )
}

/** Material3 typography holder. Screen-level composables use [ClearDuTypography]
 *  directly for pixel-accurate control; this is provided so default M3 components
 *  stay consistent. */
val ClearDuMaterialTypography = Typography()
