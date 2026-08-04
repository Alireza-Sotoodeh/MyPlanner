package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val GoodtimeColorScheme = darkColorScheme(
  primary = GoodtimePrimary,
  secondary = GoodtimeBreak,
  tertiary = GoodtimeTextSecondary,
  background = GoodtimeBackground,
  surface = GoodtimeSurface,
  onPrimary = GoodtimeBackground,
  onSecondary = GoodtimeBackground,
  onTertiary = GoodtimeBackground,
  onBackground = GoodtimeTextPrimary,
  onSurface = GoodtimeTextPrimary,
  surfaceVariant = GoodtimeSurfaceElevated,
  onSurfaceVariant = GoodtimeTextSecondary,
  outline = GoodtimeBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true for the Goodtime Dark mode experience
  dynamicColor: Boolean = false, // Disable dynamic colors to keep the beautiful dark theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) {
    GoodtimeColorScheme
  } else {
    // Elegant Light Goodtime Alternative
    lightColorScheme(
      primary = GoodtimePrimary,
      secondary = GoodtimeBreak,
      background = Color(0xFFF9F9F9),
      surface = Color(0xFFFFFFFF),
      onPrimary = Color.White,
      onSecondary = Color.White,
      onBackground = Color(0xFF111111),
      onSurface = Color(0xFF111111),
      surfaceVariant = Color(0xFFEEEEEE),
      onSurfaceVariant = Color(0xFF757575),
      outline = Color(0xFFE0E0E0)
    )
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
