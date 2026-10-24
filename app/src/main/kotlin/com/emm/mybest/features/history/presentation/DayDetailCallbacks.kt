package com.emm.mybest.features.history.presentation

internal data class DayDetailCallbacks(
    val onClose: () -> Unit,
    val onDeleteWeight: () -> Unit,
    val onDeletePhoto: (String) -> Unit,
    val onSeePhotosClick: () -> Unit,
)
