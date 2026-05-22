package com.emm.mybest.features.photo.presentation.viewer

import com.emm.mybest.domain.models.ProgressPhoto

data class PhotoViewerState(
    val photos: List<ProgressPhoto> = emptyList(),
    val initialPhotoId: String,
    val isLoading: Boolean = true,
)
