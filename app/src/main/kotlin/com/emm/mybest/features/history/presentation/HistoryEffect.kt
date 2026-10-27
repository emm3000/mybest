package com.emm.mybest.features.history.presentation

sealed interface HistoryEffect {
    data object NoteActionFailed : HistoryEffect
    data object PhotoReplaced : HistoryEffect
    data object PhotoNotFound : HistoryEffect
    data object PhotoReplaceFailed : HistoryEffect
}
