package com.example.crisisconnect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


import androidx.compose.ui.graphics.Color

// Global colors
val AppRed = Color(0xFFD32F2F)
val TextPrimary = Color(0xFF1A1A1A)   // dark readable text

private val LightColors = lightColorScheme(
    primary = PurpleStart,
    onPrimary = AppWhite,
    background = AppWhite,
    onBackground = AppBlack,
    surface = AppWhite,
    onSurface = AppBlack,
    secondary = Accent,
    onSecondary = AppWhite
)


private val DarkColors = darkColorScheme(
    primary = PurpleStart,
    onPrimary = AppWhite,
    background = AppBlack,
    onBackground = AppWhite
)

@Composable
fun CrisisConnectTheme(
    content: @Composable () -> Unit
) {
    val useDark = if (AppThemeState.darkMode) true else isSystemInDarkTheme()
    val colors = if (useDark) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
