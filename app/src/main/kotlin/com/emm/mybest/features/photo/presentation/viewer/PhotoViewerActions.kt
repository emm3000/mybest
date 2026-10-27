package com.emm.mybest.features.photo.presentation.viewer

import com.emm.mybest.domain.models.ProgressPhoto

internal data class PhotoViewerActions(
    val onBack: () -> Unit,
    val onReplace: (ProgressPhoto) -> Unit,
)
