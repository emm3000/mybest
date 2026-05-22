package com.emm.mybest.ui.components.atelier

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.AtelierInkTertiary

private val CARET_SIZE = 16.dp

/** Decorative chevron caret — ink-3 tint, no click handler. */
@Composable
fun AtelierCaret(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = null,
        modifier = modifier.size(CARET_SIZE),
        tint = AtelierInkTertiary,
    )
}
