package com.emm.mybest.features.history.presentation

import androidx.annotation.StringRes
import com.emm.mybest.domain.models.ProgressPhoto

data class PhotoRowState(
    val photo: ProgressPhoto,
    val canDelete: Boolean,
    @StringRes val descriptionRes: Int = photo.type.toDescriptionRes(),
)
