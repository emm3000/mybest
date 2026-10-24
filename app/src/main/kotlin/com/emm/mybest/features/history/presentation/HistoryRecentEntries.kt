package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.atelier.Hairline
import kotlinx.datetime.LocalDate

@Composable
internal fun HistoryRecentEntries(
    entries: List<HistoryRecentRowState>,
    onRowClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    entries.forEachIndexed { index, row ->
        HistoryRecentRow(
            row = row,
            onClick = onRowClick,
            modifier = modifier,
        )
        if (index < entries.lastIndex) {
            Hairline(inset = 28.dp)
        }
    }
}
