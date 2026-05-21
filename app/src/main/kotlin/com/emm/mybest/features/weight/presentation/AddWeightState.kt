package com.emm.mybest.features.weight.presentation

data class AddWeightState(
    val weight: String = "",
    val note: String = "",
    val weightError: String? = null,
    val lastRecordedWeight: Float? = null,
    val isLoading: Boolean = false,
)
