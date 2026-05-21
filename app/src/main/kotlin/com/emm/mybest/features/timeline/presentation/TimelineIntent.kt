package com.emm.mybest.features.timeline.presentation

sealed class TimelineIntent {
    object OnBackClick : TimelineIntent()
    data class EnterSelection(val photoId: String) : TimelineIntent()
    data class ToggleSelection(val photoId: String) : TimelineIntent()
    object ExitSelection : TimelineIntent()
    object DeleteSelected : TimelineIntent()
    object CompareSelected : TimelineIntent()
}
