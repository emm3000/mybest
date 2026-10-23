package com.emm.mybest.features.preouting.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.atelier.AtelierCaret
import com.emm.mybest.ui.components.atelier.Hairline

private val ROW_VERTICAL_PADDING = 14.dp
private val ROW_GAP = 16.dp

@Composable
internal fun ValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = ROW_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
    Hairline()
}

@Composable
internal fun SentenceRow(
    text: String,
    modifier: Modifier = Modifier,
    warning: String? = null,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth().padding(top = ROW_VERTICAL_PADDING),
        style = MaterialTheme.typography.titleMedium,
    )
    if (warning != null) {
        Text(
            text = warning,
            modifier = Modifier.fillMaxWidth().padding(top = ROW_VERTICAL_PADDING / 2),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
    Hairline(modifier = Modifier.padding(top = ROW_VERTICAL_PADDING))
}

@Composable
internal fun PromptRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = ROW_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        AtelierCaret()
    }
    Hairline()
}
