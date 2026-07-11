package com.focusreset.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF112620)
val Mint = Color(0xFF35C98B)
val SoftMint = Color(0xFFD9F8EA)
val Paper = Color(0xFFF5F7F6)
val Slate = Color(0xFF52615C)

private val LightColors = lightColorScheme(
    primary = Ink, onPrimary = Color.White, secondary = Mint, onSecondary = Ink,
    background = Paper, onBackground = Ink, surface = Color.White, onSurface = Ink,
    surfaceVariant = Color(0xFFE8EFEC), onSurfaceVariant = Slate, outline = Color(0xFF9AACA5).copy(alpha = .7f)
)

@Composable
fun FocusResetTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = Typography(), content = content)
}
