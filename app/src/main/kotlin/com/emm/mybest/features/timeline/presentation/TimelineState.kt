package com.emm.mybest.features.timeline.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.ProgressPhoto
import kotlinx.datetime.LocalDate

@Stable
data class TimelineState(
    val photosByDate: Map<LocalDate, List<ProgressPhoto>> = emptyMap(),
    val photosByMonth: Map<YearMonthValue, List<ProgressPhoto>> = emptyMap(),
    val isLoading: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
)
