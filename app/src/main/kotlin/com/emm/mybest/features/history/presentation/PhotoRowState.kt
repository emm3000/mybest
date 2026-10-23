package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.models.ProgressPhoto

data class PhotoRowState(
    val photo: ProgressPhoto,
    val canDelete: Boolean,
)
