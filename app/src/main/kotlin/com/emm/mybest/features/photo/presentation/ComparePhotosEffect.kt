package com.emm.mybest.features.photo.presentation

sealed interface ComparePhotosEffect {
    data object NavigateBack : ComparePhotosEffect
    data class ShowError(val message: String) : ComparePhotosEffect
}
