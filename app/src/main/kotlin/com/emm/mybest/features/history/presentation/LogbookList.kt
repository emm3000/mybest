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
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val HEADER_VERTICAL_PADDING = 16.dp
private val EMPTY_VERTICAL_PADDING = 48.dp
private val ROW_HAIRLINE_INSET = 28.dp

@Composable
internal fun LogbookList(
    days: ImmutableList<LogbookDayState>,
    canWriteNote: Boolean,
    callbacks: LogbookCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (canWriteNote) WriteNoteRow(onClick = callbacks.onWriteNoteClick)
        if (days.isEmpty()) LogbookEmpty() else LogbookDays(days = days, callbacks = callbacks)
    }
}

@Composable
private fun LogbookDays(days: ImmutableList<LogbookDayState>, callbacks: LogbookCallbacks) {
    days.forEachIndexed { index, day ->
        if (index > 0) Hairline()
        LogbookDayHeader(day = day)
        LogbookDayRows(day = day, callbacks = callbacks)
    }
}

@Composable
private fun WriteNoteRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HISTORY_GUT, vertical = HEADER_VERTICAL_PADDING),
    ) {
        HButton(
            text = stringResource(R.string.logbook_write_note),
            onClick = onClick,
            variant = ButtonVariant.Outline,
        )
    }
    Hairline()
}

@Composable
private fun LogbookDayRows(day: LogbookDayState, callbacks: LogbookCallbacks) {
    day.rows.forEachIndexed { index, row ->
        LogbookRow(row = row, onClick = { callbacks.onRowClick(day.appDay, row) })
        if (index < day.rows.lastIndex) Hairline(inset = ROW_HAIRLINE_INSET)
    }
}

@Composable
private fun LogbookDayHeader(day: LogbookDayState, modifier: Modifier = Modifier) {
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
private fun LogbookEmpty(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HISTORY_GUT, vertical = EMPTY_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.Center,
    ) {
        MicroLabel(
            text = stringResource(R.string.logbook_empty),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}

private val PREVIEW_CALLBACKS = LogbookCallbacks(onDayClick = {}, onNoteClick = {}, onWriteNoteClick = {})

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun LogbookListPreview() {
    AtelierTheme {
        LogbookList(days = previewLogbookDays(), canWriteNote = true, callbacks = PREVIEW_CALLBACKS)
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun LogbookListEmptyPreview() {
    AtelierTheme {
        LogbookList(days = persistentListOf(), canWriteNote = false, callbacks = PREVIEW_CALLBACKS)
    }
}
