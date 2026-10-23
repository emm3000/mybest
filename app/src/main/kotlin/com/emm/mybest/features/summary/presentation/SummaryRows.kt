package com.emm.mybest.features.summary.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.atelier.Hairline

private val ROW_PADDING = 12.dp
private val ROW_GAP = 16.dp
private val ARROW_SIZE = 18.dp

@Composable
internal fun SummaryRow(
    row: SummaryRowState,
    onPromptClick: (SummaryDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        when (row) {
            is SummaryRowState.Line -> SummaryLine(row = row)
            is SummaryRowState.SetupPrompt -> SummaryPrompt(row = row, onClick = { onPromptClick(row.destination) })
        }
        Hairline()
    }
}

@Composable
private fun SummaryLine(
    row: SummaryRowState.Line,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = ROW_PADDING),
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = row.label.resolve(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = row.value.resolve(), style = MaterialTheme.typography.titleMedium, color = row.valueColor)
    }
}

@Composable
private fun SummaryPrompt(
    row: SummaryRowState.SetupPrompt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = ROW_PADDING),
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.text.resolve(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(ARROW_SIZE),
        )
    }
}
