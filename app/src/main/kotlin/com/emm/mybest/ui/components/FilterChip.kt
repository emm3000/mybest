package com.emm.mybest.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme

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
            .semantics {
                this[SemanticsProperties.Selected] = selected
                this[SemanticsProperties.StateDescription] =
                    if (selected) "Seleccionado" else "No seleccionado"
            }
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

@Preview(showBackground = true, name = "Light Mode")
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
)
@Composable
private fun HFilterChipPreview() {
    MyBestTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HFilterChip(
                    label = "Seleccionado",
                    selected = true,
                    onClick = {},
                )
                HFilterChip(
                    label = "No seleccionado",
                    selected = false,
                    onClick = {},
                )
                HFilterChip(
                    label = "Deshabilitado",
                    selected = false,
                    enabled = false,
                    onClick = {},
                )
            }
        }
    }
}
