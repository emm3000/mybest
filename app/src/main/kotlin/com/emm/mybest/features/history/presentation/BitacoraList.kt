package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

private val HEADER_VERTICAL_PADDING = 16.dp
private val EMPTY_VERTICAL_PADDING = 48.dp
private val ROW_HAIRLINE_INSET = 28.dp

@Composable
internal fun BitacoraList(
    days: ImmutableList<BitacoraDayState>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (days.isEmpty()) {
        BitacoraEmpty(modifier = modifier)
        return
    }
    Column(modifier = modifier) {
        days.forEach { day ->
            BitacoraDayHeader(day = day)
            Hairline()
            BitacoraDayRows(day = day, onDayClick = onDayClick)
        }
    }
}

@Composable
private fun BitacoraDayRows(day: BitacoraDayState, onDayClick: (LocalDate) -> Unit) {
    day.rows.forEachIndexed { index, row ->
        BitacoraRow(row = row, onClick = { onDayClick(day.appDay) })
        if (index < day.rows.lastIndex) Hairline(inset = ROW_HAIRLINE_INSET)
    }
}

@Composable
private fun BitacoraDayHeader(day: BitacoraDayState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HISTORY_GUT, vertical = HEADER_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MicroLabel(text = day.dateLabel)
        if (day.isToday) {
            MicroLabel(
                text = stringResource(R.string.history_today_chip),
                style = MicroLabelStyle(tone = MicroLabelTone.Done),
            )
        }
    }
}

@Composable
private fun BitacoraEmpty(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HISTORY_GUT, vertical = EMPTY_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.Center,
    ) {
        MicroLabel(
            text = stringResource(R.string.bitacora_empty),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun BitacoraListPreview() {
    AtelierTheme {
        BitacoraList(days = previewBitacoraDays(), onDayClick = {})
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun BitacoraListEmptyPreview() {
    AtelierTheme {
        BitacoraList(days = persistentListOf(), onDayClick = {})
    }
}
