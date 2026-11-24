package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val StudioDarkColorScheme = darkColorScheme(
    primary = DarkStudioColors.contrastSurface,
    onPrimary = DarkStudioColors.onContrastSurface,
    primaryContainer = DarkStudioColors.cardSecondary,
    onPrimaryContainer = DarkStudioColors.textPrimary,
    secondary = BadgeHot,
    onSecondary = DarkStudioColors.textPrimary,
    secondaryContainer = DarkStudioColors.pillBg,
    onSecondaryContainer = DarkStudioColors.pillText,
    tertiary = DarkStudioColors.accent,
    onTertiary = DarkStudioColors.textPrimary,
    background = DarkStudioColors.background,
    onBackground = DarkStudioColors.textPrimary,
    surface = DarkStudioColors.card,
    onSurface = DarkStudioColors.textPrimary,
    surfaceVariant = DarkStudioColors.cardSecondary,
    onSurfaceVariant = DarkStudioColors.textSecondary,
    outline = DarkStudioColors.border,
    outlineVariant = DarkStudioColors.border,
    error = DarkStudioColors.danger,
    onError = DarkStudioColors.textPrimary
)

private val StudioLightColorScheme = lightColorScheme(
    primary = LightStudioColors.contrastSurface,
    onPrimary = LightStudioColors.onContrastSurface,
    primaryContainer = LightStudioColors.cardSecondary,
    onPrimaryContainer = LightStudioColors.textPrimary,
    secondary = BadgeHot,
    onSecondary = LightStudioColors.onContrastSurface,
    secondaryContainer = LightStudioColors.pillBg,
    onSecondaryContainer = LightStudioColors.pillText,
    tertiary = LightStudioColors.accent,
    onTertiary = LightStudioColors.onContrastSurface,
    background = LightStudioColors.background,
    onBackground = LightStudioColors.textPrimary,
    surface = LightStudioColors.card,
    onSurface = LightStudioColors.textPrimary,
    surfaceVariant = LightStudioColors.cardSecondary,
    onSurfaceVariant = LightStudioColors.textSecondary,
    outline = LightStudioColors.border,
    outlineVariant = LightStudioColors.border,
    error = LightStudioColors.danger,
    onError = LightStudioColors.onContrastSurface
)

val LocalStudioColors = staticCompositionLocalOf { LightStudioColors }

object StudioTheme {
    val colors: StudioColorPalette
        @Composable
        get() = LocalStudioColors.current
}

@Composable
fun BugtaEditzTheme(
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val studioPalette = if (isDarkTheme) DarkStudioColors else LightStudioColors
    val colorScheme = if (isDarkTheme) StudioDarkColorScheme else StudioLightColorScheme

    CompositionLocalProvider(LocalStudioColors provides studioPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
