package com.emm.mybest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ─────────────────────────────────────────────────────────────────────────────
// shadcn/ui — Neutral theme mapped to Material 3 color roles
//
// Mapping strategy:
//   M3 primary          → shadcn --primary (near-black / near-white)
//   M3 secondary        → shadcn --secondary (light gray)
//   M3 tertiary         → shadcn success green
//   M3 error            → shadcn --destructive (red)
//   M3 background/surface → shadcn --background
//   M3 surfaceVariant   → shadcn --muted (light gray bg)
//   M3 surfaceContainer → shadcn --card
//   M3 outline          → shadcn --ring (subtle ring)
//   M3 outlineVariant   → shadcn --border
// ─────────────────────────────────────────────────────────────────────────────

// Starlink rebrand: retained for reference — not applied at runtime (dark forced).
@Suppress("UnusedPrivateProperty")
private val lightScheme = lightColorScheme(
    // ── Primary (black button in shadcn) ──────────────────────────────────────
    primary = shadcnPrimary,
    onPrimary = shadcnPrimaryFg,
    primaryContainer = shadcnSecondary,
    onPrimaryContainer = shadcnPrimary,

    // ── Secondary (soft gray, "secondary" variant) ────────────────────────────
    secondary = shadcnMuted,
    onSecondary = shadcnForeground,
    secondaryContainer = shadcnSecondary,
    onSecondaryContainer = shadcnSecondaryFg,

    // ── Tertiary → success green ───────────────────────────────────────────────
    tertiary = shadcnSuccess,
    onTertiary = shadcnWhite,
    tertiaryContainer = shadcnSuccessContainer,
    onTertiaryContainer = shadcnOnSuccessContainer,

    // ── Error → destructive red ───────────────────────────────────────────────
    error = shadcnDestructive,
    onError = shadcnDestructiveFg,
    errorContainer = shadcnErrorContainer,
    onErrorContainer = shadcnOnErrorContainer,

    // ── Background & Surface ──────────────────────────────────────────────────
    background = shadcnBackground,
    onBackground = shadcnForeground,
    surface = shadcnBackground,
    onSurface = shadcnForeground,

    // ── Surface variants → muted / card tones ────────────────────────────────
    surfaceVariant = shadcnMuted,
    onSurfaceVariant = shadcnMutedFg,

    // ── Borders & rings ───────────────────────────────────────────────────────
    outline = shadcnRing,
    outlineVariant = shadcnBorder,

    // ── Inverse ───────────────────────────────────────────────────────────────
    inverseSurface = shadcnPrimary,
    inverseOnSurface = shadcnPrimaryFg,
    inversePrimary = shadcnDarkPrimary,

    scrim = shadcnBlack,

    // ── Surface containers → card / popover tones ────────────────────────────
    surfaceContainerLowest = shadcnWhite,
    surfaceContainerLow = shadcnBackground, // pure white
    surfaceContainer = shadcnSecondary, // #F5F5F5
    surfaceContainerHigh = shadcnBorder, // #E5E5E5
    surfaceContainerHighest = shadcnInput, // #E5E5E5
    surfaceDim = shadcnBorder,
    surfaceBright = shadcnWhite,
)

// Starlink rebrand: dark forced — lightScheme retained but not applied at runtime.
private val darkScheme = darkColorScheme(
    // ── Primary (white buttons on pure black) ─────────────────────────────────
    primary = starlinkOnSurface,
    onPrimary = starlinkBlack,
    primaryContainer = starlinkSurfaceHigh,
    onPrimaryContainer = starlinkOnSurface,

    // ── Secondary (muted gray) ────────────────────────────────────────────────
    secondary = starlinkMuted,
    onSecondary = starlinkBlack,
    secondaryContainer = starlinkSurface,
    onSecondaryContainer = starlinkOnSurface,

    // ── Tertiary → cyan accent ────────────────────────────────────────────────
    tertiary = starlinkAccent,
    onTertiary = starlinkBlack,
    tertiaryContainer = starlinkAccentDim,
    onTertiaryContainer = starlinkOnSurface,

    // ── Error → iOS red (high contrast on pure black) ────────────────────────
    error = starlinkError,
    onError = starlinkOnSurface,
    errorContainer = starlinkSurface,
    onErrorContainer = starlinkError,

    // ── Background & Surface ──────────────────────────────────────────────────
    background = starlinkBlack,
    onBackground = starlinkOnSurface,
    surface = starlinkBlack,
    onSurface = starlinkOnSurface,

    // ── Surface variants ──────────────────────────────────────────────────────
    surfaceVariant = starlinkSurface,
    onSurfaceVariant = starlinkMuted,

    // ── Borders ───────────────────────────────────────────────────────────────
    outline = starlinkOutline,
    outlineVariant = starlinkBorder,

    // ── Inverse ───────────────────────────────────────────────────────────────
    inverseSurface = starlinkOnSurface,
    inverseOnSurface = starlinkBlack,
    inversePrimary = starlinkBlack,

    scrim = starlinkBlack,

    // ── Surface containers ────────────────────────────────────────────────────
    surfaceContainerLowest = starlinkBlack,
    surfaceContainerLow = starlinkSurface,
    surfaceContainer = starlinkSurface,
    surfaceContainerHigh = starlinkSurfaceHigh,
    surfaceContainerHighest = starlinkOutline,
    surfaceDim = starlinkBlack,
    surfaceBright = starlinkSurfaceHigh,
)

@Composable
fun MyBestTheme(
    // Starlink rebrand: dark forced — parameter kept for API compatibility.
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = darkScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
