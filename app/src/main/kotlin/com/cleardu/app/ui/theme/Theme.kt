package com.cleardu.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
 * 背景亮度自适应的文字颜色。
 *
 * 当天气背景为高亮度类型（晴天、雪天、多云白天）时自动切换为深色文字，
 * 确保文字在亮色背景上清晰可读；暗色背景时保持原有的浅色文字。
 *
 * @return 当前背景亮度下适用的前景色/文字色
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
            isBright = true
        )
    } else {
        BackgroundAwareColors(
            foreground = LiquidGlassColors.Foreground,
            text400 = LiquidGlassColors.Text400,
            text300 = LiquidGlassColors.Text300,
            glassBg = LiquidGlassColors.GlassBg,
            glassBorder = LiquidGlassColors.GlassBorderSubtle,
            isBright = false
        )
    }
}

/**
 * 背景亮度感知的颜色集合。
 */
data class BackgroundAwareColors(
    val foreground: androidx.compose.ui.graphics.Color,
    val text400: androidx.compose.ui.graphics.Color,
    val text300: androidx.compose.ui.graphics.Color,
    val glassBg: androidx.compose.ui.graphics.Color,
    val glassBorder: androidx.compose.ui.graphics.Color,
    val isBright: Boolean
)
