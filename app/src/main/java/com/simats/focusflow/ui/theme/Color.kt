package com.simats.focusflow.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Default Dark Palette
val DarkBackground = Color(0xFF0F0F14)
val DarkSurface = Color(0xFF181722)
val DarkSurfaceVariant = Color(0xFF201F2D)
val DarkBorder = Color(0xFF262433)
val DarkPrimary = Color(0xFF8B5CF6)
val DarkPrimaryLight = Color(0xFFC4B5FD)
val DarkTextPrimary = Color(0xFFF1F1F5)
val DarkTextMuted = Color(0xFF9CA3AF)

// Clean Light Palette
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF3F4F6)
val LightBorder = Color(0xFFE5E7EB)
val LightPrimary = Color(0xFF7C3AED)
val LightPrimaryLight = Color(0xFF8B5CF6)
val LightTextPrimary = Color(0xFF111827)
val LightTextMuted = Color(0xFF6B7280)

// Semantic Accents
val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)

// Legacy Material Tokens (kept for compatibility)
val Purple80 = DarkPrimaryLight
val PurpleGrey80 = DarkTextMuted
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = LightPrimary
val PurpleGrey40 = LightTextMuted
val Pink40 = Color(0xFF7D5260)

@Immutable
data class FocusFlowColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val primary: Color,
    val primaryLight: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val isDark: Boolean
)

val DarkFocusFlowColors = FocusFlowColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    border = DarkBorder,
    primary = DarkPrimary,
    primaryLight = DarkPrimaryLight,
    textPrimary = DarkTextPrimary,
    textMuted = DarkTextMuted,
    isDark = true
)

val LightFocusFlowColors = FocusFlowColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    border = LightBorder,
    primary = LightPrimary,
    primaryLight = LightPrimaryLight,
    textPrimary = LightTextPrimary,
    textMuted = LightTextMuted,
    isDark = false
)
