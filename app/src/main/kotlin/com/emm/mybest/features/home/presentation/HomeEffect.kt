package com.emm.mybest.features.home.presentation

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect
}
