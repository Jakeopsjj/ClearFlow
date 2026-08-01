package com.cleardu.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
