package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BoldTypographyColorScheme = darkColorScheme(
    primary = ThemePrimary,
    onPrimary = ThemeSecondary,
    secondary = ThemeSecondary,
    onSecondary = ThemePrimary,
    background = ThemeBackground,
    onBackground = ThemeText,
    surface = ThemeSurface,
    onSurface = ThemeText,
    outline = ThemeBorder,
    error = ThemeWarning,
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Prefer our beautiful selected design theme
    content: @Composable () -> Unit,
) {
    // Force our high-contrast design color scheme for both states to retain thematic styling
    MaterialTheme(
        colorScheme = BoldTypographyColorScheme,
        typography = Typography,
        content = content
    )
}
