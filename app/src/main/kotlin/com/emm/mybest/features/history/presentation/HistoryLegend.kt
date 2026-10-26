package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone

private const val LEGEND_COLUMNS = 2
private val LEGEND_ROW_GAP = 10.dp
private val LEGEND_ITEM_GAP = 8.dp

@Composable
internal fun HistoryLegend(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HISTORY_GUT)
            .padding(top = 12.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(LEGEND_ROW_GAP),
    ) {
        HistoryMarkKind.entries.chunked(LEGEND_COLUMNS).forEach { kinds ->
            Row(horizontalArrangement = Arrangement.spacedBy(LEGEND_ITEM_GAP)) {
                kinds.forEach { LegendItem(kind = it, modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun LegendItem(kind: HistoryMarkKind, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(LEGEND_ITEM_GAP),
    ) {
        HistoryMark(kind = kind, modifier = Modifier.alignBy { it.measuredHeight })
        MicroLabel(
            text = stringResource(kind.legendRes()),
            modifier = Modifier.alignBy(FirstBaseline),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}
