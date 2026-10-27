package com.emm.mybest.features.summary.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.atelier.Hairline

private val ROW_PADDING = 12.dp
private val ROW_GAP = 16.dp
private val ARROW_SIZE = 18.dp
private val LABEL_MIN_WIDTH = 140.dp

@Composable
internal fun SummaryRow(
    row: SummaryRowState,
    onRowClick: (SummaryDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        when (row) {
            is SummaryRowState.Line -> SummaryLine(row = row, onClick = onRowClick)
            is SummaryRowState.SetupPrompt -> SummaryPrompt(row = row, onClick = { onRowClick(row.destination) })
        }
        Hairline()
    }
}

@Composable
private fun SummaryLine(
    row: SummaryRowState.Line,
    onClick: (SummaryDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .then(row.destination.toClickModifier(onClick))
            .padding(vertical = ROW_PADDING),
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
        verticalArrangement = Arrangement.spacedBy(ROW_GAP),
        itemVerticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = row.label.resolve(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).widthIn(min = LABEL_MIN_WIDTH),
        )
        Text(text = row.value.resolve(), style = MaterialTheme.typography.titleMedium, color = row.valueColor)
    }
}

private fun SummaryDestination?.toClickModifier(onClick: (SummaryDestination) -> Unit): Modifier =
    if (this == null) Modifier else Modifier.clickable(role = Role.Button, onClick = { onClick(this) })

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
