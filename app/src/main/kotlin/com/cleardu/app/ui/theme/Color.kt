package com.cleardu.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color tokens ported verbatim from the Liquid Glass design (HTML :root).
 *
 * Naming intentionally mirrors the CSS variable names so a designer can audit
 * one-to-one. Values are kept as raw 0xAARRGGBB literals for traceability
 * instead of being abstracted behind semantic aliases here.
 *
 * Format: 0xAARRGGBB where AA = alpha (0x00 transparent → 0xFF opaque).
 */
object LiquidGlassColors {

    // ===== Brand scale =====
    val Brand50 = Color(0xFFE8F2FF)
    val Brand100 = Color(0xFFCFE5FF)
    val Brand200 = Color(0xFF9FCBFF)
    val Brand300 = Color(0xFF66ABFF)
    val Brand400 = Color(0xFF2E8DFF)
    val Brand500 = Color(0xFF007AFF)
    val Brand600 = Color(0xFF0064D6)
    val Brand700 = Color(0xFF004FAD)
    val Brand800 = Color(0xFF003B82)
    val Brand900 = Color(0xFF00275A)

    // ===== Medical extension colors =====
    val MedicalCyan = Color(0xFF5AC8FA)
    val MedicalTeal = Color(0xFF30B0C7)
    val MedicalBlue = Color(0xFF007AFF)
    val MedicalGreen = Color(0xFF34C759)
    val MedicalRed = Color(0xFFFF3B30)
    val MedicalOrange = Color(0xFFFF9500)
    val MedicalPurple = Color(0xFFAF52DE)
    val MedicalPink = Color(0xFFFF2D55)
    val MedicalYellow = Color(0xFFFFCC00)
    val MedicalIndigo = Color(0xFF5856D6)

    // ===== Foreground / text scale (dark-first) =====
    val Foreground = Color(0xFFF5F5F7)
    val Text100 = Color(0xFFE3E3E8)
    val Text200 = Color(0xFFC7C7CC)
    val Text300 = Color(0xFFAEAEB2)
    val Text400 = Color(0xFF8E8E93)
    val Text500 = Color(0xFF6E6E73)
    val Text600 = Color(0xB3FFFFFF) // rgba(255,255,255,0.7)
    val Text700 = Color(0xFF3C3C43)
    val Text800 = Color(0xFF1D1D1F)

    // ===== Mesh gradient stops (rgba with alpha) =====
    val MeshPurple = Color(0x665856D6)        // rgba(88,86,214,0.4)
    val MeshCyan = Color(0x405AC8FA)          // rgba(90,200,250,0.25)
    val MeshDeepPurple = Color(0x33AF52DE)    // rgba(175,82,222,0.2)
    val MeshBlue = Color(0x33007AFF)          // rgba(0,122,255,0.2)
    val MeshAdditionalGreen = Color(0x2634C759) // rgba(52,199,89,0.15)
    val MeshAdditionalOrange = Color(0x1AFF9500) // rgba(255,149,0,0.1)

    // ===== Glass material tokens (dark-first) =====
    val GlassBg = Color(0x1FFFFFFF)          // rgba(255,255,255,0.12)
    val GlassBgStrong = Color(0x2EFFFFFF)    // rgba(255,255,255,0.18)
    val GlassBgLight = Color(0x14FFFFFF)     // rgba(255,255,255,0.08)
    val GlassBorder = Color(0x38FFFFFF)      // rgba(255,255,255,0.22)
    val GlassBorderSubtle = Color(0x1FFFFFFF) // rgba(255,255,255,0.12)
    val GlassHighlight = Color(0x59FFFFFF)   // rgba(255,255,255,0.35)
    val GlassShadow = Color(0x40000000)      // rgba(0,0,0,0.25)
    val GlassStrongShadow = Color(0x66000000) // rgba(0,0,0,0.4)
    val GlassSpecularTop = Color(0x40FFFFFF) // rgba(255,255,255,0.25)
    val GlassSpecularMid = Color(0x0DFFFFFF) // rgba(255,255,255,0.05)
    val GlassSpecularMidLow = Color(0x08FFFFFF) // rgba(255,255,255,0.03)
    val GlassEdgeHighlight = Color(0x66FFFFFF) // rgba(255,255,255,0.4)

    // ===== Tinted backgrounds per color =====
    val TintCyanBg = Color(0x265AC8FA)        // rgba(90,200,250,0.15)
    val TintCyanBorder = Color(0x4D5AC8FA)   // rgba(90,200,250,0.3)
    val TintOrangeBg = Color(0x26FF9500)     // rgba(255,149,0,0.15)
    val TintOrangeBorder = Color(0x4DFF9500)  // rgba(255,149,0,0.3)
    val TintPurpleBg = Color(0x26AF52DE)      // rgba(175,82,222,0.15)
    val TintPurpleBorder = Color(0x4DAF52DE)  // rgba(175,82,222,0.3)
    val TintGreenBg = Color(0x1F34C759)       // rgba(52,199,89,0.12)
    val TintGreenStrong = Color(0x2634C759)   // rgba(52,199,89,0.15)
    val TintGreenBorder = Color(0x3334C759)   // rgba(52,199,89,0.2)
    val TintIndigoBg = Color(0x265856D6)      // rgba(88,86,214,0.15)
    val TintIndigoBorder = Color(0x4D5856D6)  // rgba(88,86,214,0.3)
    val TintRedBg = Color(0x1FFF3B30)         // rgba(255,59,48,0.12)
    val TintRedBorder = Color(0x4DFF3B30)     // rgba(255,59,48,0.3)

    // ===== Critical card gradient stops =====
    val CriticalRedTint = Color(0x14FF3B30)   // rgba(255,59,48,0.08)
    val CriticalOrangeTint = Color(0x0FFF9500) // rgba(255,149,0,0.06)
    val CriticalBadgeBg = Color(0x1FFF3B30)   // rgba(255,59,48,0.12)

    // ===== Start button gradient stops =====
    val StartGradientStart = MedicalCyan      // #5ac8fa
    val StartGradientMid = MedicalBlue         // #007aff
    val StartGradientEnd = Color(0xFF0064D6)  // #0064d6

    // ===== Page background (dark-first) =====
    val Background = Color(0xFF000000)
    val BackgroundAlternative = Color(0xFF1A1A1A)

    // ===== Dashboard-specific tinted backgrounds =====
    val TintCyanLight = Color(0x1A5AC8FA)      // rgba(90,200,250,0.1)
    val TintCyanMd = Color(0x335AC8FA)         // rgba(90,200,250,0.2)
    val TintCyanStrong = Color(0x405AC8FA)     // rgba(90,200,250,0.25)
    val TintCyanActive = Color(0x595AC8FA)     // rgba(90,200,250,0.35)
    val TintCyanGlow = Color(0x665AC8FA)       // rgba(90,200,250,0.4)
    val TintCyanGlowStrong = Color(0x805AC8FA) // rgba(90,200,250,0.5)
    val TintCyanBar = Color(0x995AC8FA)        // rgba(90,200,250,0.6)

    val TintBlueBg = Color(0x1F007AFF)         // rgba(0,122,255,0.12)
    val TintBlueGlow = Color(0x4D007AFF)       // rgba(0,122,255,0.3)
    val TintBlueGlowStrong = Color(0x80007AFF) // rgba(0,122,255,0.5)
    val TintBlueShadow = Color(0x4D007AFF)     // rgba(0,122,255,0.3)

    val TintRedStrong = Color(0x2EFF3B30)      // rgba(255,59,48,0.18)
    val TintRedGlow = Color(0x4DFF3B30)        // rgba(255,59,48,0.3)
    val TintRedGlowStrong = Color(0x66FF3B30)  // rgba(255,59,48,0.4)
    val TintRedShadow = Color(0x33FF3B30)      // rgba(255,59,48,0.2)
    val TintRedLight = Color(0xFFFF6B60)       // #ff6b60

    val TintGreenGlow = Color(0x6634C759)      // rgba(52,199,89,0.4)
    val TintGreenGlowStrong = Color(0x8034C759) // rgba(52,199,89,0.5)
    val TintGreenBarStart = Color(0x4D34C759)  // rgba(52,199,89,0.3)
    val TintGreenBarEnd = Color(0x9934C759)    // rgba(52,199,89,0.6)

    val TintOrangeStrong = Color(0x26FF9500)   // rgba(255,149,0,0.15)
    val TintOrangeActive = Color(0x66FF9500)   // rgba(255,149,0,0.4)
    val TintOrangeGlow = Color(0x26FF9500)     // rgba(255,149,0,0.15)
    val TintOrangeGlowDot = Color(0x80FF9500)  // rgba(255,149,0,0.5)
    val TintOrangeBarStart = Color(0x4DFF9500) // rgba(255,149,0,0.3)
    val TintOrangeText = Color(0xB3FF9500)     // rgba(255,149,0,0.7)

    // ===== Vital-specific colors =====
    val BpNormal = Color(0xFF34C759)
    val BpElevated = Color(0xFFFFCC00)
    val BpHigh = Color(0xFFFF9500)
    val BpCrisis = Color(0xFFFF3B30)
    val HrNormal = Color(0xFF30D158)
    val HrElevated = Color(0xFFFF9F0A)
    val HrDanger = Color(0xFFFF453A)
    val FluidGood = Color(0xFF5AC8FA)
    val FluidWarning = Color(0xFFFF9F0A)
    val FluidDanger = Color(0xFFFF453A)

    // ===== Chart / graph colors =====
    val ChartGrid = Color(0x14FFFFFF)          // rgba(255,255,255,0.08)
    val ChartTargetLine = Color(0x99FF9500)    // rgba(255,149,0,0.6)
    val ChartFillStart = Color(0x4D5AC8FA)     // rgba(90,200,250,0.3)
    val ChartFillEnd = Color(0x005AC8FA)       // rgba(90,200,250,0)

    // ===== Navigation bar colors =====
    val NavBg = Color(0x14FFFFFF)              // rgba(255,255,255,0.08)
    val NavBorder = Color(0x33FFFFFF)          // rgba(255,255,255,0.2)
    val NavShadow = Color(0x33000000)          // rgba(0,0,0,0.2)
    val NavSpecular = Color(0x4DFFFFFF)        // rgba(255,255,255,0.3)
    val NavBlurFadeStart = Color(0x26000000)   // rgba(0,0,0,0.15)
    val NavBlurFadeMid = Color(0x0D000000)     // rgba(0,0,0,0.05)
    val NavIndicator = MedicalCyan
    val NavIconInactive = Text400
    val NavIconActive = MedicalCyan

    // ===== Card active state =====
    val CardActiveBg = Color(0x29FFFFFF)       // rgba(255,255,255,0.16)

    // ===== Divider =====
    val DividerSubtle = Color(0x0FFFFFFF)      // rgba(255,255,255,0.06)
    val DividerLight = Color(0x14FFFFFF)       // rgba(255,255,255,0.08)
    val DividerMedium = Color(0x1AFFFFFF)      // rgba(255,255,255,0.1)

    // ===== Track / progress =====
    val TrackBg = Color(0x1AFFFFFF)            // rgba(255,255,255,0.1)

    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
}
