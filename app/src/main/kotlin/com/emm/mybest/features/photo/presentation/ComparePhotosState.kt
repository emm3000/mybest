package com.emm.mybest.features.photo.presentation

import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto

data class ComparePhotosState(
    val photos: List<ProgressPhoto> = emptyList(),
    val selectedType: PhotoType? = null,
    val beforePhoto: ProgressPhoto? = null,
    val afterPhoto: ProgressPhoto? = null,
    val totalPhotosCount: Int = 0,
    val photoCountByType: Map<PhotoType, Int> = emptyMap(),
    val isLoading: Boolean = false,
)
