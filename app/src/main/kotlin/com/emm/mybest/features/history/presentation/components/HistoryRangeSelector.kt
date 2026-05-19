package com.emm.mybest.features.history.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.features.history.presentation.HistoryRange

private val SEGMENT_HEIGHT = 32.dp

@Composable
fun HistoryRangeSelector(
    selectedRange: HistoryRange,
    onRangeChange: (HistoryRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        HistoryRange.WEEK to "Sem",
        HistoryRange.MONTH to "Mes",
        HistoryRange.YEAR to "Año",
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .height(SEGMENT_HEIGHT),
    ) {
        options.forEachIndexed { index, (range, label) ->
            SegmentedButton(
                selected = selectedRange == range,
                onClick = { onRangeChange(range) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                icon = {},
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
