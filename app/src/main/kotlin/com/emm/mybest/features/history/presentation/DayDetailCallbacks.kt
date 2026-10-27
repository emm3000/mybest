package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.models.ProgressPhoto

internal data class DayDetailCallbacks(
    val onClose: () -> Unit,
    val onDeleteWeight: () -> Unit,
    val onDeletePhoto: (String) -> Unit,
    val onReplacePhoto: (ProgressPhoto) -> Unit,
    val onSeePhotosClick: () -> Unit,
)
