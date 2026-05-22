package com.emm.mybest.features.photo.presentation

import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.usecase.photo.PhotoTypeOverview
import kotlinx.datetime.LocalDate

data class PhotosState(
    val selectedType: PhotoType = PhotoType.TRUNK,
    val overview: PhotoTypeOverview = emptyPhotoTypeOverview(),
    val countByType: Map<PhotoType, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val today: LocalDate,
)

internal fun emptyPhotoTypeOverview(): PhotoTypeOverview = PhotoTypeOverview(
    before = null,
    after = null,
    timeline = emptyList(),
    beforeWeightKg = null,
    afterWeightKg = null,
)
