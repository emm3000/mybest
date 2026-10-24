package com.emm.mybest.features.photo.presentation.components

import androidx.compose.runtime.Immutable

@Immutable
internal data class PhotoTileModel(
    val photoPath: String?,
    val dateLabel: String,
    val isNewest: Boolean,
)
