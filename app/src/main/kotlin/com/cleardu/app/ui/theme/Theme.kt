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
 * 统一液态玻璃视觉参数。
 *
 * 以首页仪表盘 GlassCard 默认参数为唯一视觉基准，所有页面共享同一套玻璃参数。
 * 当天气背景为高亮度时，通过 [glassParams] 动态覆写为亮色适配值。
 *
 * @param background 玻璃背景色（仪表盘基准：GlassBg = rgba(255,255,255,0.12)）
 * @param border 玻璃边框色（仪表盘基准：GlassBorder = rgba(255,255,255,0.22)）
 * @param shadowColor 阴影颜色（仪表盘基准：Color.Transparent = 无阴影）
 * @param shadowElevation 阴影高度dp（仪表盘基准：0f = 无阴影）
 * @param specularTop 顶部高光色（仪表盘基准：GlassSpecularTop = rgba(255,255,255,0.25)）
 */
data class GlassParams(
    val background: Color,
    val border: Color,
    val shadowColor: Color,
    val shadowElevation: Float,
    val specularTop: Color
)

/**
 * 获取当前背景亮度下的液态玻璃参数。
 *
 * 亮背景（晴天/雪天等）：浅灰磨砂卡片 + 白色文字，卡片略暗于背景形成层次。
 * 暗背景（夜间/阴天等）：浅白磨砂卡片 + 黑色文字，卡片略亮于背景形成层次。
 *
 * @return 当前适用的玻璃视觉参数
 */
@Composable
fun glassParams(): GlassParams {
    val weatherState by WeatherBackgroundManager.state.collectAsState()
    val isBright = weatherState.enabled && weatherState.isBrightBackground

    return if (isBright) {
        // 亮背景 → 浅灰磨砂卡片，卡片略暗于背景，白色文字
        GlassParams(
            background = LiquidGlassColors.BrightGlassBg,
            border = LiquidGlassColors.BrightGlassBorder,
            shadowColor = Color.Transparent,
            shadowElevation = 0f,
            specularTop = LiquidGlassColors.BrightGlassSpecular
        )
    } else {
        // 暗背景 → 浅白磨砂卡片，卡片略亮于背景，黑色文字
        GlassParams(
            background = LiquidGlassColors.GlassBg,
            border = LiquidGlassColors.GlassBorder,
            shadowColor = Color.Transparent,
            shadowElevation = 0f,
            specularTop = LiquidGlassColors.GlassSpecularTop
        )
    }
}

/**
 * 背景亮度自适应的文字颜色。
 *
 * 亮背景（晴天、雪天、多云白天）：白色文字，确保在浅灰卡片/亮背景上清晰可读。
 * 暗背景（夜间、阴天）：黑色文字，确保在浅白卡片/暗背景上清晰可读。
 *
 * @return 当前背景亮度下适用的前景色/文字色
 */
@Composable
fun backgroundAwareColors(): BackgroundAwareColors {
    val weatherState by WeatherBackgroundManager.state.collectAsState()
    val isBright = weatherState.enabled && weatherState.isBrightBackground

    return if (isBright) {
        // 亮背景 → 白色文字
        BackgroundAwareColors(
            foreground = LiquidGlassColors.Foreground,
            text400 = LiquidGlassColors.Text400,
            text300 = LiquidGlassColors.Text300,
            glassBg = LiquidGlassColors.BrightGlassBg,
            glassBorder = LiquidGlassColors.BrightGlassBorder,
            isBright = true
        )
    } else {
        // 暗背景 → 黑色文字
        BackgroundAwareColors(
            foreground = LiquidGlassColors.DarkForeground,
            text400 = LiquidGlassColors.DarkText400,
            text300 = LiquidGlassColors.DarkText300,
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
