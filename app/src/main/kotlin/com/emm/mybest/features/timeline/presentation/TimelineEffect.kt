package com.emm.mybest.features.timeline.presentation

sealed class TimelineEffect {
    object NavigateBack : TimelineEffect()
    object NavigateToCompare : TimelineEffect()
}
