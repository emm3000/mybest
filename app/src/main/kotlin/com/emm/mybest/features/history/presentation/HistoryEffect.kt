package com.emm.mybest.features.history.presentation

sealed interface HistoryEffect {
    data object NoteActionFailed : HistoryEffect
}
