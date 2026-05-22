package com.emm.mybest.features.photo.presentation

sealed interface PhotosEffect {
    data class ShowError(val message: String) : PhotosEffect
    data class NavigateToCompare(val beforeId: String, val afterId: String) : PhotosEffect
}
