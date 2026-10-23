package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Spoiled Store Official Design Tokens (Theme-aware for Dark Mode & High Contrast Light Mode)
val BackgroundLight: Color @Composable get() = MaterialTheme.colorScheme.background
val SurfaceLight: Color @Composable get() = MaterialTheme.colorScheme.surface
val SurfaceSecondary: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val SurfaceTertiary: Color @Composable get() = if (LocalHighContrastMode.current) Color(0xFFE2E8F0) else Color(0xFF1E283A)

val TextPrimary: Color @Composable get() = MaterialTheme.colorScheme.onBackground
val TextSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val TextTertiary: Color @Composable get() = if (LocalHighContrastMode.current) Color(0xFF1E293B) else Color(0xFF94A3B8)
val TextDisabled: Color @Composable get() = if (LocalHighContrastMode.current) Color(0xFF64748B) else Color(0xFF475569)

val AppleBlue: Color @Composable get() = MaterialTheme.colorScheme.primary
val AppleBlueHover = Color(0xFF0066D6)
val AppleBlueSoft: Color @Composable get() = if (LocalHighContrastMode.current) Color(0xFFD4E5FF) else Color(0xFF263248)
val AppleBlueBorder: Color @Composable get() = if (LocalHighContrastMode.current) Color(0xFF000000) else Color(0xFF3B4861)

val BorderLight: Color @Composable get() = MaterialTheme.colorScheme.outline
val BorderStrong: Color @Composable get() = if (LocalHighContrastMode.current) Color(0xFF000000) else Color(0xFF334155)

val SuccessGreen = Color(0xFF10B981)
val WarningGold = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)

// Featured Card Slate/Charcoal Gradient
val CharcoalCardStart = Color(0xFF1E293B)
val CharcoalCardMid = Color(0xFF334155)
val CharcoalCardEnd = Color(0xFF0F172A)

// Translucent & Glass Accents
val GlassWhite: Color @Composable get() = if (LocalHighContrastMode.current) Color(0xFFFFFFFF) else Color(0xDE1A2234)
val GlassWhiteStrong: Color @Composable get() = if (LocalHighContrastMode.current) Color(0xFFFFFFFF) else Color(0xF01A2234)
val GlassBorder: Color @Composable get() = if (LocalHighContrastMode.current) Color(0xFF000000) else Color(0xFF2A364F)

