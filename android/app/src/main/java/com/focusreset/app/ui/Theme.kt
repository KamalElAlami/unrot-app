package com.focusreset.app.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Paper = Color(0xFF0B1728)
val CardNavy = Color(0xFF182C45)
val RaisedNavy = Color(0xFF213A58)
val Ink = Color(0xFFF6F8FC)
val Mint = Color(0xFF2ED9A3)
val SoftMint = Color(0xFF123C37)
val Slate = Color(0xFF94A4B8)
val Coral = Color(0xFFFF6B6B)
val Amber = Color(0xFFFFBF5B)

private val FocusColors = darkColorScheme(
    primary = Mint, onPrimary = Paper, secondary = Mint, onSecondary = Paper,
    background = Paper, onBackground = Ink, surface = CardNavy, onSurface = Ink,
    surfaceVariant = RaisedNavy, onSurfaceVariant = Slate, outline = Color(0xFF58708C)
)

@Composable
fun FocusResetTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FocusColors, typography = Typography(), content = content)
}
