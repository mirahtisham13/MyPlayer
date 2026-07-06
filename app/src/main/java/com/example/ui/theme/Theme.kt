package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AmoledColorScheme = darkColorScheme(
  primary = AmoledPrimary,
  secondary = AmoledSecondary,
  background = AmoledBlack,
  surface = AmoledSurface,
  onBackground = AmoledOnBackground,
  onSurface = AmoledOnSurface,
  outline = AmoledOutline,
  outlineVariant = AmoledOutlineVariant
)

private val LightColorScheme = lightColorScheme(
  primary = Color.Black,
  secondary = Color(0xFF636366),
  background = Color.White,
  surface = Color(0xFFF2F2F7),
  onBackground = Color.Black,
  onSurface = Color.Black,
  outline = Color(0xFFE5E5EA),
  outlineVariant = Color(0xFFF2F2F7)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) AmoledColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
