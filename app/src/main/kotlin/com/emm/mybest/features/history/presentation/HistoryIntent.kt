package com.emm.mybest.features.history.presentation

import com.emm.mybest.core.datetime.YearMonthValue
import kotlinx.datetime.LocalDate

sealed class HistoryIntent {
    sealed interface NoteIntent
    sealed interface NoteEditIntent : NoteIntent
    sealed interface NoteDeleteIntent : NoteIntent

    data class OnMonthChange(val newMonth: YearMonthValue) : HistoryIntent()
    data class OnDateSelected(val date: LocalDate) : HistoryIntent()
    object OnDateDismiss : HistoryIntent()
    data class OnDeleteWeight(val date: LocalDate) : HistoryIntent()
    data class OnDeletePhoto(val photoId: String) : HistoryIntent()
    data class OnViewSelected(val view: HistoryView) : HistoryIntent()
    data object OnWriteNoteClick : HistoryIntent(), NoteEditIntent
    data class OnNoteClick(val noteId: Long) : HistoryIntent(), NoteEditIntent
    data class OnNoteTextChange(val text: String) : HistoryIntent(), NoteEditIntent
    data object OnNoteSave : HistoryIntent(), NoteEditIntent
    data object OnNoteDismiss : HistoryIntent(), NoteEditIntent
    data object OnNoteDeleteClick : HistoryIntent(), NoteDeleteIntent
    data object OnNoteDeleteConfirm : HistoryIntent(), NoteDeleteIntent
    data object OnNoteDeleteDismiss : HistoryIntent(), NoteDeleteIntent
}
