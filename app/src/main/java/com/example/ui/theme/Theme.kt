package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val CleanWhiteColorScheme = lightColorScheme(
  primary = BluePrimary,
  onPrimary = SurfaceWhite,
  primaryContainer = BlueLight,
  onPrimaryContainer = BlueDark,
  secondary = BlueSecondary,
  onSecondary = SurfaceWhite,
  secondaryContainer = BlueLight,
  onSecondaryContainer = BlueDark,
  tertiary = TealSlate,
  onTertiary = SurfaceWhite,
  tertiaryContainer = TealLight,
  onTertiaryContainer = TealSlate,
  background = BackgroundClean,
  onBackground = TextPrimary,
  surface = SurfaceWhite,
  onSurface = TextPrimary,
  surfaceVariant = BackgroundClean,
  onSurfaceVariant = TextSecondary,
  outline = BorderSubtle,
  error = FinanceRed,
  onError = SurfaceWhite,
  errorContainer = FinanceRedLight,
  onErrorContainer = FinanceRed
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Pure white theme as requested by user
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val currentDensity = LocalDensity.current
  // Cap fontScale safely to preserve fit-and-finish and prevent text overflowing across lines on devices
  val safeDensity = Density(
    density = currentDensity.density,
    fontScale = currentDensity.fontScale.coerceIn(0.80f, 1.0f)
  )

  CompositionLocalProvider(LocalDensity provides safeDensity) {
    MaterialTheme(
      colorScheme = CleanWhiteColorScheme,
      typography = Typography,
      content = content
    )
  }
}

