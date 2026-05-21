package com.emm.mybest.features.photo.presentation

sealed class ComparePhotosEffect {
    data class ShowError(val message: String) : ComparePhotosEffect()
}
