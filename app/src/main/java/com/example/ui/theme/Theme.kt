package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyanGlow,
    onPrimary = NavyDark,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = Color.White,
    secondary = BrandTealLight,
    onSecondary = NavyDark,
    secondaryContainer = CardDark,
    onSecondaryContainer = ElectricCyanLight,
    tertiary = PrimaryBlueLight,
    onTertiary = Color.White,
    background = NavyDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = Color(0xFF1E293B),
    error = StatusError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1D4ED8),
    secondary = BrandTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0FDFA),
    onSecondaryContainer = Color(0xFF0F766E),
    tertiary = ElectricCyan,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardLightSubtle,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = BorderLightStrong,
    error = StatusError,
    onError = Color.White
)

@Composable
fun ReturnRouteTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = ReturnRouteTheme(darkTheme = darkTheme, content = content)
