package com.emm.mybest.features.photo.presentation

sealed class AddPhotoEffect {
    object NavigateBack : AddPhotoEffect()
    data class ShowError(val message: String) : AddPhotoEffect()
}
