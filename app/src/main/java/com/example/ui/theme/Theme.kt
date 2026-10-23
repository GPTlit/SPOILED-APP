package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalHighContrastMode = staticCompositionLocalOf { false }

private val HighContrastLightColorScheme = lightColorScheme(
    primary = Color(0xFF003EA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E5FF),
    onPrimaryContainer = Color(0xFF002266),
    secondary = Color(0xFF000000),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFF0F172A),
    onTertiary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF000000),
    outline = Color(0xFF000000),
    outlineVariant = Color(0xFF1E293B)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF263248),
    onPrimaryContainer = Color(0xFFA5B4FC),
    secondary = Color(0xFF818CF8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = CharcoalCardMid,
    onTertiary = Color.White,
    background = Color(0xFF090D16),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF131A29),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1C2438),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF263248),
    outlineVariant = Color(0xFF334155)
)

@Composable
fun SpoiledStoreTheme(
    isHighContrastLight: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isHighContrastLight) HighContrastLightColorScheme else DarkColorScheme

    CompositionLocalProvider(LocalHighContrastMode provides isHighContrastLight) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
