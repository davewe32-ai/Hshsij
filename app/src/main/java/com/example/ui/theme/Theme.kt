package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    primaryContainer = HighlightLightBlue,
    onPrimaryContainer = NavyBlue,
    secondary = NavyBlue,
    onSecondary = Color.White,
    secondaryContainer = HighlightLightBlue,
    onSecondaryContainer = NavyBlue,
    tertiary = AccentOrange,
    onTertiary = Color.White,
    background = BackgroundClean,
    onBackground = TextCharcoal,
    surface = SurfaceLightGray,
    onSurface = TextCharcoal,
    surfaceVariant = HighlightLightBlue,
    onSurfaceVariant = NavyBlue,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = Color.Black,
    primaryContainer = NavyBlue,
    onPrimaryContainer = Color.White,
    secondary = SkyBlue,
    onSecondary = Color.Black,
    secondaryContainer = NavyBlue,
    onSecondaryContainer = Color.White,
    tertiary = AccentOrange,
    onTertiary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = SkyBlue,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to enforce Glorious Homes branding consistently
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
