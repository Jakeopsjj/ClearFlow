package com.cleardu.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
}

/** Material3 typography holder. Screen-level composables use [ClearDuTypography]
 *  directly for pixel-accurate control; this is provided so default M3 components
 *  stay consistent. */
val ClearDuMaterialTypography = Typography()
