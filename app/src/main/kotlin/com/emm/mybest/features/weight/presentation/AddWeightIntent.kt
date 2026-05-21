package com.emm.mybest.features.weight.presentation

sealed class AddWeightIntent {
    data class OnWeightChange(val weight: String) : AddWeightIntent()
    data class OnNoteChange(val note: String) : AddWeightIntent()
    object OnSaveClick : AddWeightIntent()
}
