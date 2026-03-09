package com.emm.mybest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

private const val FILTER_CHIP_DISABLED_ALPHA = 0.5f

@Composable
fun HFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val cs = MaterialTheme.colorScheme
    val containerColor = if (selected) cs.secondaryContainer else cs.background
    val contentColor = if (selected) cs.onSecondaryContainer else cs.onSurface
    val borderColor = if (selected) cs.outline else cs.outlineVariant

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minHeight = 32.dp)
            .alpha(if (enabled) 1f else FILTER_CHIP_DISABLED_ALPHA),
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(
                PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ),
        )
    }
}
