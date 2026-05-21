package com.emm.mybest.features.photo.presentation

import com.emm.mybest.domain.models.PhotoType

data class SelectedPhoto(
    val uri: String,
    val type: PhotoType = PhotoType.FACE,
)
