package com.example.dailytasks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

private fun onAccentColor(accent: Color): Color =
    if (accent.luminance() > 0.55f) Color(0xFF161C24) else Color(0xFFFFFFFF)

private fun lightColors(accent: Color): ColorScheme = lightColorScheme(
    primary = accent,
    onPrimary = onAccentColor(accent),
    primaryContainer = lerp(accent, Color.White, 0.82f),
    onPrimaryContainer = lerp(accent, Color.Black, 0.38f),
    secondary = Color(0xFF4F6076),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E3F0),
    onSecondaryContainer = Color(0xFF0F1D2E),
    tertiary = GreenSuccess,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = GreenSuccessContainer,
    onTertiaryContainer = Color(0xFF003825),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = Color(0xFF7B8798),
    outlineVariant = LightOutlineVariant,
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = Color(0xFF2D3137),
    inverseOnSurface = Color(0xFFF1F3F8),
    inversePrimary = lerp(accent, Color.White, 0.45f),
    scrim = Color.Black
)

private fun darkColors(accent: Color): ColorScheme = darkColorScheme(
    primary = lerp(accent, Color.White, 0.55f),
    onPrimary = lerp(accent, Color.Black, 0.7f),
    primaryContainer = lerp(accent, Color.Black, 0.45f),
    onPrimaryContainer = lerp(accent, Color.White, 0.9f),
    secondary = Color(0xFFB7C7DC),
    onSecondary = Color(0xFF213143),
    secondaryContainer = Color(0xFF34465B),
    onSecondaryContainer = Color(0xFFD3E3F9),
    tertiary = DarkGreenSuccess,
    onTertiary = Color(0xFF003825),
    tertiaryContainer = DarkGreenSuccessContainer,
    onTertiaryContainer = Color(0xFFD7F5E4),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = Color(0xFF8492A5),
    outlineVariant = DarkOutlineVariant,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE2E8F2),
    inverseOnSurface = Color(0xFF2C3138),
    inversePrimary = accent,
    scrim = Color.Black
)

@Composable
fun DailyTaskTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accent: Color = AccentOptions.first().color,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) darkColors(accent) else lightColors(accent),
        typography = AppTypography,
        content = content
    )
}
