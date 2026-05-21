package com.emm.mybest.features.photo.presentation

import com.emm.mybest.domain.models.PhotoType

sealed class AddPhotoIntent {
    data class OnPhotosSelected(val uris: List<String>) : AddPhotoIntent()
    data class OnTypeSelected(val index: Int, val type: PhotoType) : AddPhotoIntent()
    data class OnRemovePhoto(val index: Int) : AddPhotoIntent()
    object OnSaveClick : AddPhotoIntent()
}
