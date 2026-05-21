package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.models.ProgressPhoto

internal data class DayTimelineEntry(
    val type: DayTimelineEventType,
    val sequence: Long,
    val photo: ProgressPhoto? = null,
)
