package com.emm.mybest.features.photo.presentation

import androidx.compose.runtime.Stable

@Stable
data class PhotosCallbacks(
    val onBack: () -> Unit,
    val onOpenViewer: (String) -> Unit,
    val onCompare: (String, String) -> Unit,
)
