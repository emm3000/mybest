package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.domain.usecase.history.HistoryRecentEntry
import com.emm.mybest.ui.components.atelier.Hairline
import kotlinx.datetime.LocalDate

@Composable
internal fun HistoryRecentEntries(
    entries: List<HistoryRecentEntry>,
    today: LocalDate,
    onRowClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    entries.forEachIndexed { index, entry ->
        HistoryRecentRow(
            entry = entry,
            today = today,
            onClick = onRowClick,
            modifier = modifier,
        )
        if (index < entries.lastIndex) {
            Hairline(inset = 28.dp)
        }
    }
}
