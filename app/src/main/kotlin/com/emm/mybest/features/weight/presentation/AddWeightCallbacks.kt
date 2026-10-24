package com.emm.mybest.features.weight.presentation

internal data class AddWeightCallbacks(
    val onBackClick: () -> Unit,
    val onIntent: (AddWeightIntent) -> Unit,
)
