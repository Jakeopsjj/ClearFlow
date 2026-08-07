package com.cleardu.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.cleardu.app.data.weather.WeatherBackgroundManager

/**
 * Material3 color scheme aligned with the Liquid Glass dark-first palette.
 *
 * The reference design is dark-first; light mode is supported but the
 * onboarding screen visually locks to dark-first regardless of system theme
 * because the mesh background and glass tokens assume a dark substrate.
 */
private val DarkColors = darkColorScheme(
    primary = LiquidGlassColors.MedicalCyan,
    onPrimary = LiquidGlassColors.White,
    primaryContainer = LiquidGlassColors.MedicalBlue,
    onPrimaryContainer = LiquidGlassColors.White,
    secondary = LiquidGlassColors.GlassBgStrong,
    onSecondary = LiquidGlassColors.Foreground,
    tertiary = LiquidGlassColors.MedicalPurple,
    onTertiary = LiquidGlassColors.White,
    background = LiquidGlassColors.Background,
    onBackground = LiquidGlassColors.Foreground,
    surface = LiquidGlassColors.BackgroundAlternative,
    onSurface = LiquidGlassColors.Foreground,
    surfaceVariant = LiquidGlassColors.GlassBg,
    onSurfaceVariant = LiquidGlassColors.Text300,
    outline = LiquidGlassColors.GlassBorder,
    outlineVariant = LiquidGlassColors.GlassBorderSubtle,
    error = LiquidGlassColors.MedicalRed,
    onError = LiquidGlassColors.White,
)

private val LightColors = lightColorScheme(
    primary = LiquidGlassColors.MedicalBlue,
    onPrimary = LiquidGlassColors.White,
    background = LiquidGlassColors.Background,
    onBackground = LiquidGlassColors.Foreground,
    surface = LiquidGlassColors.BackgroundAlternative,
    onSurface = LiquidGlassColors.Foreground,
)

@Composable
fun ClearDuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ClearDuMaterialTypography,
        content = content
    )
}

/**
 * 背景亮度自适应的颜色集合。
 *
 * 当天气背景为高亮度类型（晴天、雪天、多云白天）时：
 * - 玻璃底色加深（深色磨砂），适度提升阴影深度
 * - 文字/图标使用深色
 * - 导航栏使用与卡片一致的深色玻璃参数
 *
 * 暗色背景时保持原有默认参数，文字/图标使用浅色。
 *
 * @return 当前背景亮度下适用的完整颜色集合
 */
@Composable
fun backgroundAwareColors(): BackgroundAwareColors {
    val weatherState by WeatherBackgroundManager.state.collectAsState()
    val isBright = weatherState.enabled && weatherState.isBrightBackground

    return if (isBright) {
        BackgroundAwareColors(
            foreground = LiquidGlassColors.BrightForeground,
            text400 = LiquidGlassColors.BrightText400,
            text300 = LiquidGlassColors.BrightText300,
            glassBg = LiquidGlassColors.BrightGlassBg,
            glassBorder = LiquidGlassColors.BrightGlassBorder,
            glassSpecularTop = LiquidGlassColors.BrightGlassSpecularTop,
            glassShadow = LiquidGlassColors.BrightGlassShadow,
            navBg = LiquidGlassColors.BrightNavBg,
            navBorder = LiquidGlassColors.BrightNavBorder,
            navSpecular = LiquidGlassColors.BrightNavSpecular,
            navBlurFadeStart = LiquidGlassColors.BrightNavBlurFadeStart,
            navBlurFadeMid = LiquidGlassColors.BrightNavBlurFadeMid,
            isBright = true
        )
    } else {
        BackgroundAwareColors(
            foreground = LiquidGlassColors.Foreground,
            text400 = LiquidGlassColors.Text400,
            text300 = LiquidGlassColors.Text300,
            glassBg = LiquidGlassColors.GlassBg,
            glassBorder = LiquidGlassColors.GlassBorderSubtle,
            glassSpecularTop = LiquidGlassColors.GlassSpecularTop,
            glassShadow = Color.Transparent,
            navBg = LiquidGlassColors.NavBg,
            navBorder = LiquidGlassColors.NavBorder,
            navSpecular = LiquidGlassColors.NavSpecular,
            navBlurFadeStart = LiquidGlassColors.NavBlurFadeStart,
            navBlurFadeMid = LiquidGlassColors.NavBlurFadeMid,
            isBright = false
        )
    }
}

/**
 * 背景亮度感知的颜色集合。
 * 包含卡片玻璃、导航栏、文字三组动态参数，确保全局统一。
 */
data class BackgroundAwareColors(
    val foreground: androidx.compose.ui.graphics.Color,
    val text400: androidx.compose.ui.graphics.Color,
    val text300: androidx.compose.ui.graphics.Color,
    val glassBg: androidx.compose.ui.graphics.Color,
    val glassBorder: androidx.compose.ui.graphics.Color,
    val glassSpecularTop: androidx.compose.ui.graphics.Color,
    val glassShadow: androidx.compose.ui.graphics.Color,
    val navBg: androidx.compose.ui.graphics.Color,
    val navBorder: androidx.compose.ui.graphics.Color,
    val navSpecular: androidx.compose.ui.graphics.Color,
    val navBlurFadeStart: androidx.compose.ui.graphics.Color,
    val navBlurFadeMid: androidx.compose.ui.graphics.Color,
    val isBright: Boolean
)
