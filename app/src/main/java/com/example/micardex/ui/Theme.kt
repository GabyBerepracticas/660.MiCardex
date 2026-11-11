package com.example.micardex.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple900 = Color(0xFF4A148C)
val Purple700 = Color(0xFF6A1B9A)
val Purple200 = Color(0xFFCE93D8)
val Purple50 = Color(0xFFF3E5F5)
val Blue900 = Color(0xFF0D47A1)
val Blue700 = Color(0xFF1565C0)
val Blue500 = Color(0xFF1E88E5)
val Blue100 = Color(0xFFBBDEFB)
val Blue50 = Color(0xFFE3F2FD)
val Lavender = Color(0xFFF5F0FB)

/** Degradado morado → azul usado en encabezados. */
val GradienteCardex = Brush.horizontalGradient(listOf(Purple700, Blue700))

private val LightColors = lightColorScheme(
    primary = Purple700,
    onPrimary = Color.White,
    primaryContainer = Purple50,
    onPrimaryContainer = Purple900,
    secondary = Blue700,
    onSecondary = Color.White,
    secondaryContainer = Blue50,
    onSecondaryContainer = Blue900,
    tertiary = Blue500,
    background = Lavender,
    surface = Color.White,
    surfaceVariant = Purple50,
    onSurfaceVariant = Purple900
)

private val DarkColors = darkColorScheme(
    primary = Purple200,
    onPrimary = Purple900,
    primaryContainer = Purple700,
    onPrimaryContainer = Purple50,
    secondary = Blue100,
    onSecondary = Blue900,
    secondaryContainer = Blue700,
    onSecondaryContainer = Blue50,
    tertiary = Blue500,
    background = Color(0xFF1A1030),
    surface = Color(0xFF241A3D),
    surfaceVariant = Color(0xFF33265A),
    onSurfaceVariant = Purple50
)

@Composable
fun CardexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
