package com.qadam.lastpuff.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GreenPrimary = Color(0xFF2E9E6A)
private val GreenSecondary = Color(0xFF5BB98C)
private val GreenTertiary = Color(0xFF1B7A52)
private val CoralAccent = Color(0xFFE87461)
private val SoftBackground = Color(0xFFF5F7F6)
private val SoftSurface = Color(0xFFFFFFFF)
private val DarkBackground = Color(0xFF121816)
private val DarkSurface = Color(0xFF1C2420)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F0E3),
    onPrimaryContainer = Color(0xFF0A3D28),
    secondary = GreenSecondary,
    onSecondary = Color.White,
    tertiary = CoralAccent,
    onTertiary = Color.White,
    background = SoftBackground,
    onBackground = Color(0xFF1A1F1C),
    surface = SoftSurface,
    onSurface = Color(0xFF1A1F1C),
    surfaceVariant = Color(0xFFE8EFEB),
    onSurfaceVariant = Color(0xFF4A554F),
    outline = Color(0xFFB8C5BD)
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenSecondary,
    onPrimary = Color(0xFF0A2E1E),
    primaryContainer = Color(0xFF1B5C3F),
    onPrimaryContainer = Color(0xFFD4F0E3),
    secondary = Color(0xFF7DD4A8),
    onSecondary = Color(0xFF0A2E1E),
    tertiary = CoralAccent,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = Color(0xFFE8EDEA),
    surface = DarkSurface,
    onSurface = Color(0xFFE8EDEA),
    surfaceVariant = Color(0xFF2A332E),
    onSurfaceVariant = Color(0xFFB8C5BD),
    outline = Color(0xFF4A554F)
)

@Composable
fun QadamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QadamTypography,
        content = content
    )
}
