package com.simats.focusflow.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF07060D)
val DarkSurface = Color(0xFF11101C)
val DarkSurfaceVariant = Color(0xFF181628)
val DarkBorder = Color(0xFF232035)
val DarkPrimary = Color(0xFF8B5CF6)
val DarkPrimaryLight = Color(0xFFC4B5FD)
val DarkTextPrimary = Color(0xFFF1F1F5)
val DarkTextMuted = Color(0xFF94A3B8)

val AccentCyan = Color(0xFF38BDF8)
val WarmCoral = Color(0xFFFF5A5F)
val AmbientGlowViolet = Color(0xFF1A103C)
val AmbientGlowDeepBlue = Color(0xFF0D1B2A)

val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF3F4F6)
val LightBorder = Color(0xFFE5E7EB)
val LightPrimary = Color(0xFF7C3AED)
val LightPrimaryLight = Color(0xFF8B5CF6)
val LightTextPrimary = Color(0xFF111827)
val LightTextMuted = Color(0xFF6B7280)

val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)

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
    val isDark: Boolean,
    val accentCyan: Color = AccentCyan,
    val accentCoral: Color = WarmCoral
) {
    val cardBorderBrush: Brush
        get() = if (isDark) {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.14f),
                    Color.White.copy(alpha = 0.03f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    border.copy(alpha = 0.8f),
                    border.copy(alpha = 0.3f)
                )
            )
        }
}

val DarkFocusFlowColors = FocusFlowColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    border = DarkBorder,
    primary = DarkPrimary,
    primaryLight = DarkPrimaryLight,
    textPrimary = DarkTextPrimary,
    textMuted = DarkTextMuted,
    isDark = true,
    accentCyan = AccentCyan,
    accentCoral = WarmCoral
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
    isDark = false,
    accentCyan = AccentCyan,
    accentCoral = WarmCoral
)
