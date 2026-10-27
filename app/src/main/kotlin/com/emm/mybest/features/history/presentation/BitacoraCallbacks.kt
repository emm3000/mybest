package com.emm.mybest.features.history.presentation

import kotlinx.datetime.LocalDate

internal data class BitacoraCallbacks(
    val onDayClick: (LocalDate) -> Unit,
    val onNoteClick: (Long) -> Unit,
    val onWriteNoteClick: () -> Unit,
) {

    fun onRowClick(appDay: LocalDate, row: BitacoraRowState) {
        val noteId = row.noteId
        if (noteId == null) onDayClick(appDay) else onNoteClick(noteId)
    }
}
