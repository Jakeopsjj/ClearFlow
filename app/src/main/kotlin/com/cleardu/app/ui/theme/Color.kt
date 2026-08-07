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
    val NavBg = Color(0xE61A1A1A)              // rgba(26,26,26,0.9) — opaque frosted
    val NavBorder = Color(0x4DFFFFFF)           // rgba(255,255,255,0.3)
    val NavShadow = Color(0x66000000)           // rgba(0,0,0,0.4)
    val NavSpecular = Color(0x33FFFFFF)         // rgba(255,255,255,0.2)
    val NavBlurFadeStart = Color(0x80000000)    // rgba(0,0,0,0.5)
    val NavBlurFadeMid = Color(0x33000000)      // rgba(0,0,0,0.2)
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

    // ===== Data record: segmented control =====
    val SegIndicatorStart = Color(0x595AC8FA)    // rgba(90,200,250,0.35)
    val SegIndicatorEnd = Color(0x4D007AFF)       // rgba(0,122,255,0.3)
    val SegIndicatorBorder = Color(0x665AC8FA)     // rgba(90,200,250,0.4)
    val SegIndicatorShadow = Color(0x335AC8FA)     // rgba(90,200,250,0.2)
    val SegInactiveText = Text600                 // rgba(255,255,255,0.7)

    // ===== Data record: keypad =====
    val KeyBg = GlassBgLight                       // rgba(255,255,255,0.08)
    val KeyActiveBg = Color(0x38FFFFFF)            // rgba(255,255,255,0.22)
    val KeyText = Foreground                        // #f5f5f7
    val KeyActionColor = MedicalCyan                // #5ac8fa

    // ===== Data record: placeholders =====
    val PlaceholderText = Color(0x59F5F5F7)        // rgba(245,245,247,0.35)
    val PlaceholderInput = Color(0xFF666666)       // 深灰色，浅色背景可见
    val PlaceholderInputStrong = Color(0xB38E8E93) // rgba(142,142,147,0.7)

    // ===== Data record: save button glow =====
    val BtnGlowCyan = Color(0x595AC8FA)            // rgba(90,200,250,0.35)
    val BtnGlowBlue = Color(0x4D007AFF)            // rgba(0,122,255,0.3)
    val BtnGlowCyanActive = Color(0x405AC8FA)      // rgba(90,200,250,0.25)
    val BtnGlowBlueActive = Color(0x33007AFF)      // rgba(0,122,255,0.2)

    // ===== Data record: input ring pulse =====
    val GlowCyanSoft = Color(0x335AC8FA)           // rgba(90,200,250,0.2)
    val GlowCyanStrong = Color(0x665AC8FA)         // rgba(90,200,250,0.4)

    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)

    // ===== 背景亮度自适应文字颜色 =====
    // 高亮度背景（晴天、雪天、多云白天）时使用深色文字，确保清晰可读
    val BrightForeground = Color(0xFF1D1D1F)     // 深色主文字
    val BrightText400 = Color(0xFF3C3C43)        // 深色副文字
    val BrightText300 = Color(0xFF48484A)        // 深色辅助文字
    val BrightGlassBg = Color(0x40000000)         // rgba(0,0,0,0.25) — 深色磨砂卡片
    val BrightGlassBorder = Color(0x33000000)     // rgba(0,0,0,0.2) — 深色卡片边框

    // ===== Light mode tokens (medication page) =====
    val LightBackground = Color(0xFFF2F2F7)
    val LightForeground = Color(0xFF1D1D1F)
    val LightGlassBg = Color(0xCCFFFFFF)       // rgba(255,255,255,0.8) — 提高不透明度，使卡片在浅色背景上呈现明显磨砂质感
    val LightGlassBgStrong = Color(0xE6FFFFFF)  // rgba(255,255,255,0.9) — 强磨砂效果
    val LightGlassBorder = Color(0x59D1D6DC)    // rgba(209,214,220,0.35) — 淡灰蓝边框，在白色背景上可见
    val LightGlassShadow = Color(0x26000000)    // rgba(0,0,0,0.15) — 增强阴影深度
    val LightGlassSpecularTop = Color(0x99FFFFFF) // rgba(255,255,255,0.6)
    val LightGlassSpecularMid = Color(0x33FFFFFF)  // rgba(255,255,255,0.2)
    val LightGlassSpecularMidLow = Color(0x1AFFFFFF) // rgba(255,255,255,0.1)

    // Light-mode tinted backgrounds
    val LightTintCyanBg = Color(0x1A007AFF)       // rgba(0,122,255,0.1)
    val LightTintCyanMd = Color(0x1F007AFF)       // rgba(0,122,255,0.12)
    val LightTintCyanActive = Color(0x33007AFF)   // rgba(0,122,255,0.2)
    val LightTintCyanBorder = Color(0x40007AFF)   // rgba(0,122,255,0.25)
    val LightTintOrangeBg = Color(0x1AFF9500)     // rgba(255,149,0,0.1)
    val LightTintOrangeIcon = Color(0x1FFF9500)   // rgba(255,149,0,0.12)
    val LightTintOrangeBtn = Color(0x26FF9500)    // rgba(255,149,0,0.15)
    val LightTintOrangeBorder = Color(0x4DFF9500)  // rgba(255,149,0,0.3)
    val LightTintOrangeText = Color(0xB3FF9500)    // rgba(255,149,0,0.7)
    val LightTintPurpleBg = Color(0x1FAF52DE)     // rgba(175,82,222,0.12)
    val LightTintGreenBg = Color(0x1434C759)      // rgba(52,199,89,0.08)
    val LightTintGreenStrong = Color(0x1F34C759)   // rgba(52,199,89,0.12)
    val LightTintGreenBorder = Color(0x3334C759)   // rgba(52,199,89,0.2)
    val LightTintIndigoBg = Color(0x1F5856D6)     // rgba(88,86,214,0.12)
    val LightTintRedBg = Color(0x14FF3B30)         // rgba(255,59,48,0.08)

    // Light-mode nav bar
    val LightNavBg = Color(0x14FFFFFF)            // rgba(255,255,255,0.08)
    val LightNavBorder = Color(0x33FFFFFF)         // rgba(255,255,255,0.2)
    val LightNavSpecular = Color(0x33FFFFFF)       // rgba(255,255,255,0.2)
    val LightNavBlurFadeStart = Color(0x26000000)   // rgba(0,0,0,0.15)
    val LightNavBlurFadeMid = Color(0x0D000000)     // rgba(0,0,0,0.05)

    // Light-mode FAB
    val LightFabBg = Color(0x33007AFF)             // rgba(0,122,255,0.2)
    val LightFabBorder = Color(0x4D007AFF)         // rgba(0,122,255,0.3)

    // Light-mode mesh gradient stops — 增强光斑强度，使浅色背景上的玻璃卡片透光效果更明显
    val LightMeshPurple = Color(0x995856D6)        // 左下紫色光斑，alpha 60%
    val LightMeshCyan = Color(0x805AC8FA)          // 顶部青色光斑，alpha 50%
    val LightMeshDeepPurple = Color(0x66AF52DE)    // 右下深紫色光斑，alpha 40%
    val LightMeshBlue = Color(0x66007AFF)          // 右上蓝色光斑，alpha 40%
    val LightMeshCyanExtra = Color(0x40007AFF)     // 左中青色补充，alpha 25%

    // Light-mode dividers
    val LightDividerDot = Color(0x26000000)        // rgba(0,0,0,0.15)
    val LightCircleEmptyBorder = Color(0x26000000)  // rgba(0,0,0,0.15)
    val LightCircleEmptyBg = Color(0x0A000000)      // rgba(0,0,0,0.04)
    val LightMuted = Color(0x08000000)              // rgba(0,0,0,0.03)
    val LightBorderMedium = Color(0x1A000000)       // rgba(0,0,0,0.1)

    // ===== Light-mode tokens: reminder center page =====
    val DestructiveLight = Color(0xFFFF6B60)          // #ff6b60
    val ToggleOff = Color(0x52787880)                  // rgba(120,120,128,0.32)
    val ToggleThumb = Color(0xFFFFFFFF)
    val ToggleOn = Color(0xFF5AC8FA)                    // medical-cyan
    val LightTintRedBorder = Color(0x40FF3B30)         // rgba(255,59,48,0.25)
    val LightTintRedShadow = Color(0x1FFF3B30)         // rgba(255,59,48,0.12)
    val LightTintRedGlow = Color(0x33FF3B30)            // rgba(255,59,48,0.2)
    val LightTintRedShadowStrong = Color(0x4DFF3B30)   // rgba(255,59,48,0.3)
    val LightDividerSubtle = Color(0x0F000000)          // rgba(0,0,0,0.06)
    val LightText600 = Color(0x99000000)                // rgba(0,0,0,0.6)
    val LightGlassBgLight = Color(0x80FFFFFF)           // rgba(255,255,255,0.5)
    val LightTintBlueGlow = Color(0x33007AFF)           // rgba(0,122,255,0.2)
}
