package com.emm.mybest.features.weight.presentation

sealed class AddWeightEffect {
    object NavigateBack : AddWeightEffect()
    object OpenSettings : AddWeightEffect()
    data class ShowError(val message: String) : AddWeightEffect()
}
