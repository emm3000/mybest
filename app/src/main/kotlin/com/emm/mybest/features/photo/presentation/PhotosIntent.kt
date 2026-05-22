package com.emm.mybest.features.photo.presentation

import com.emm.mybest.domain.models.PhotoType

sealed interface PhotosIntent {
    data class SelectType(val type: PhotoType) : PhotosIntent
    data class OnPhotoCaptured(val uri: String) : PhotosIntent
    data class DeletePhoto(val id: String) : PhotosIntent
    data object OpenCompare : PhotosIntent
}
