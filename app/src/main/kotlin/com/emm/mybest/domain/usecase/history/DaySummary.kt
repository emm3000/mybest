package com.emm.mybest.domain.usecase.history

import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import kotlinx.datetime.LocalDate

data class DaySummary(
    val date: LocalDate,
    val weight: WeightEntry? = null,
    val photos: List<ProgressPhoto> = emptyList(),
) {
    val hasWeight: Boolean get() = weight != null
    val hasPhoto: Boolean get() = photos.isNotEmpty()
    val hasActivity: Boolean get() = hasWeight || hasPhoto
}
