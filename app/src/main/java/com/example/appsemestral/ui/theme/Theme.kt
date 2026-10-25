package com.example.appsemestral.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Morado = Color(0xFF6E45C7)
private val Turquesa = Color(0xFF00AFA3)
private val Magenta = Color(0xFFD8469D)
private val Fondo = Color(0xFFF7F5FC)
private val Texto = Color(0xFF211B2D)
private val LightColors = lightColorScheme(
    primary = Morado, onPrimary = Color.White, secondary = Turquesa, onSecondary = Color.White,
    tertiary = Magenta, background = Fondo, onBackground = Texto, surface = Color.White, onSurface = Texto,
    error = Color(0xFFB3261E)
)
private val DarkColors = darkColorScheme(primary = Color(0xFFC8B5FF), secondary = Color(0xFF6DE3D9), tertiary = Color(0xFFFFA9D7))

@Composable
fun Wim100Theme(dynamicColor: Boolean = false, content: @Composable () -> Unit) {
    // La identidad corporativa no depende del fondo de pantalla del dispositivo.
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = androidx.compose.material3.Typography(), content = content)
}
