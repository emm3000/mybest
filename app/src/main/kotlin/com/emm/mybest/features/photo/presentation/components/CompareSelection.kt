package com.emm.mybest.features.photo.presentation.components

import androidx.compose.runtime.Immutable
import com.emm.mybest.domain.models.ProgressPhoto
import kotlinx.datetime.LocalDate

@Immutable
internal data class CompareSelection(
    val beforePhoto: ProgressPhoto?,
    val afterPhoto: ProgressPhoto?,
    val beforeWeightKg: Float?,
    val afterWeightKg: Float?,
    val today: LocalDate,
)
