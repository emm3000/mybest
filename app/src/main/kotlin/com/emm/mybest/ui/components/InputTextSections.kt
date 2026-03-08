package com.emm.mybest.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun InputLabelSection(label: String?, isError: Boolean) {
    if (label == null) return
    val cs = MaterialTheme.colorScheme
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = if (isError) cs.error else cs.onSurface,
        )
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
internal fun InputHelperSection(helperText: String?, isError: Boolean) {
    if (helperText == null) return
    val cs = MaterialTheme.colorScheme
    Column {
        Spacer(Modifier.height(4.dp))
        Text(
            text = helperText,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) cs.error else cs.onSurfaceVariant,
        )
    }
}
