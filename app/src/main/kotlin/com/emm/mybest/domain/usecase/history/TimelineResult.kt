package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.ProgressPhoto
import kotlinx.datetime.LocalDate

data class TimelineResult(
    val photosByDate: Map<LocalDate, List<ProgressPhoto>>,
    val photosByMonth: Map<YearMonthValue, List<ProgressPhoto>>,
)
