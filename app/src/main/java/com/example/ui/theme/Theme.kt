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

private val DarkColorScheme =
  darkColorScheme(
    primary = DeepWaterPrimary,
    secondary = DeepWaterSecondary,
    tertiary = DeepWaterTertiary,
    background = WaterBackgroundDark,
    surface = WaterSurfaceDark,
    surfaceVariant = WaterHighlightDark,
    onPrimary = WaterOnPrimaryDark,
    onBackground = WaterOnBackgroundDark,
    onSurface = WaterOnBackgroundDark,
    onSurfaceVariant = WaterOnSurfaceVariantDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = WaterPrimary,
    secondary = WaterSecondary,
    tertiary = WaterTertiary,
    background = WaterBackgroundLight,
    surface = WaterSurfaceLight,
    surfaceVariant = WaterHighlightLight,
    onPrimary = WaterOnPrimaryLight,
    onBackground = WaterOnBackgroundLight,
    onSurface = WaterOnBackgroundLight,
    onSurfaceVariant = WaterOnSurfaceVariantLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors by default to enforce our premium immersive design consistency
  dynamicColor: Boolean = false,
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
