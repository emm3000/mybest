package com.emm.mybest.features.photo.presentation

import com.emm.mybest.domain.models.ProgressPhoto

data class ComparePhotosState(
    val before: ProgressPhoto? = null,
    val after: ProgressPhoto? = null,
    val isLoading: Boolean = true,
)
