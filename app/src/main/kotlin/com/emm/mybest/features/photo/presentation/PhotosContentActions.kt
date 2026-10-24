package com.emm.mybest.features.photo.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.domain.models.PhotoType

@Stable
internal data class PhotosContentActions(
    val onSelectType: (PhotoType) -> Unit,
    val onOpenViewer: (String) -> Unit,
    val onCompare: () -> Unit,
)
