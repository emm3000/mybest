package com.emm.mybest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AtelierError = Color(0xFFE85D5D)

private val atelierScheme = darkColorScheme(
    background = AtelierBackground,
    onBackground = AtelierInk,
    surface = AtelierBackground,
    onSurface = AtelierInk,
    surfaceVariant = AtelierBackgroundElevated,
    onSurfaceVariant = AtelierInkSecondary,
    surfaceContainer = AtelierBackgroundSheet,
    surfaceContainerLow = AtelierBackgroundSheet,
    surfaceContainerLowest = AtelierBackground,
    surfaceContainerHigh = AtelierBackgroundElevated,
    surfaceContainerHighest = AtelierBackgroundElevated,
    primary = AtelierDone,
    onPrimary = AtelierBackground,
    primaryContainer = AtelierDoneDim,
    onPrimaryContainer = AtelierInk,
    secondary = AtelierInkSecondary,
    onSecondary = AtelierBackground,
    secondaryContainer = AtelierBackgroundElevated,
    onSecondaryContainer = AtelierInk,
    tertiary = AtelierWarm,
    onTertiary = AtelierBackground,
    tertiaryContainer = AtelierBackgroundElevated,
    onTertiaryContainer = AtelierInk,
    error = AtelierError,
    onError = AtelierInk,
    errorContainer = AtelierBackgroundElevated,
    onErrorContainer = AtelierError,
    outline = AtelierInkTertiary,
    outlineVariant = AtelierHairline,
    inverseSurface = AtelierInk,
    inverseOnSurface = AtelierBackground,
    inversePrimary = AtelierDone,
    scrim = Color.Black,
)

@Composable
fun AtelierTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = atelierScheme,
        typography = AtelierTypography,
        shapes = AtelierShapes,
        content = content,
    )
}
