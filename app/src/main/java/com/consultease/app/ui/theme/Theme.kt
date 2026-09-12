package com.consultease.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Color_White = androidx.compose.ui.graphics.Color(0xFFFFFFFF)

private val ConsultEaseColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color_White,
    primaryContainer = PrimaryTint,
    onPrimaryContainer = PrimaryDark,
    secondary = Accent,
    onSecondary = Color_White,
    secondaryContainer = AccentTint,
    onSecondaryContainer = Accent,
    background = Background,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = InkSoft,
    error = Danger,
    errorContainer = DangerTint,
    outline = Border
)

val ConsultEaseTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontSize = 15.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
)

@Composable
fun ConsultEaseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ConsultEaseColorScheme,
        typography = ConsultEaseTypography,
        content = content
    )
}
