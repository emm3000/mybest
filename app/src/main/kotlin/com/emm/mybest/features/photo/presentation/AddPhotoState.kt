package com.emm.mybest.features.photo.presentation

data class AddPhotoState(
    val selectedPhotos: List<SelectedPhoto> = emptyList(),
    val isLoading: Boolean = false,
)
