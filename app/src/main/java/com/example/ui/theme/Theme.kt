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

private val DarkColorScheme =
  darkColorScheme(
    primary = WarmAmberPrimaryDark,
    onPrimary = WarmAmberOnPrimaryDark,
    primaryContainer = WarmAmberContainerDark,
    onPrimaryContainer = Color(0xFFFFDCC1),
    secondary = Color(0xFFDEC2B0),
    onSecondary = Color(0xFF3F2D20),
    secondaryContainer = Color(0xFF574335),
    onSecondaryContainer = Color(0xFFFCDDC8),
    tertiary = Color(0xFFC8C997),
    onTertiary = Color(0xFF30320D),
    background = PaperBackgroundDark,
    onBackground = PaperOnBackgroundDark,
    surface = PaperSurfaceDark,
    onSurface = PaperOnSurfaceDark,
    surfaceVariant = PaperSurfaceVariantDark,
    outline = PaperOutlineDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = WarmAmberPrimary,
    onPrimary = WarmAmberOnPrimary,
    primaryContainer = WarmAmberContainer,
    onPrimaryContainer = WarmAmberOnContainer,
    secondary = WarmSecondary,
    onSecondary = WarmOnSecondary,
    secondaryContainer = WarmSecondaryContainer,
    onSecondaryContainer = WarmOnSecondaryContainer,
    tertiary = WarmTertiary,
    onTertiary = WarmOnTertiary,
    tertiaryContainer = WarmTertiaryContainer,
    onTertiaryContainer = WarmOnTertiaryContainer,
    background = PaperBackgroundLight,
    onBackground = PaperOnBackgroundLight,
    surface = PaperSurfaceLight,
    onSurface = PaperOnSurfaceLight,
    surfaceVariant = PaperSurfaceVariantLight,
    outline = PaperOutlineLight,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
