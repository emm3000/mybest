package com.emm.mybest.domain.usecase.history

import com.emm.mybest.domain.models.PhotoType
import kotlinx.datetime.LocalDate

data class HistoryRecentEntry(
    val date: LocalDate,
    val weight: Float?,
    val photoTypes: List<PhotoType>,
)
