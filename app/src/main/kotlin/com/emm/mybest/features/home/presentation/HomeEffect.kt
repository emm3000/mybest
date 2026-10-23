package com.emm.mybest.features.home.presentation

sealed interface HomeEffect {
    data object NavigateToSettings : HomeEffect
}
