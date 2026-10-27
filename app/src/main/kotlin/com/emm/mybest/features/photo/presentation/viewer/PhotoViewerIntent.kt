package com.emm.mybest.features.photo.presentation.viewer

sealed interface PhotoViewerIntent {
    data class OnReplacementCaptured(val photoId: String, val uri: String) : PhotoViewerIntent
}
