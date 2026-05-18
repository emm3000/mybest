package com.emm.mybest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Starlink-style uppercase label with wider tracking.
 * Apply via `Text(text = "DAILY", style = MaterialTheme.typography.labelMedium.uppercaseTracked())`
 * or use the property accessors in [StarlinkTextStyles] for the canonical pairings.
 *
 * NOTE: this only returns the TextStyle. Convert the string to uppercase at the call site
 * via `"daily".uppercase()` — keeping the casing transformation explicit avoids surprising
 * localized data (numbers, units) and lets translators control casing per locale.
 */
fun TextStyle.uppercaseTracked(): TextStyle = copy(letterSpacing = (letterSpacing.value + 0.4f).sp)

/** Canonical tracked uppercase styles, mapped to label tokens. */
object StarlinkTextStyles {
    val sectionLabel: TextStyle
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp)

    val chipLabel: TextStyle
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp)
}
