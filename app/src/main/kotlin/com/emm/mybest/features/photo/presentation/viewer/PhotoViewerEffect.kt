package com.emm.mybest.features.photo.presentation.viewer

sealed interface PhotoViewerEffect {
    data object PhotoReplaced : PhotoViewerEffect

    data object ReplaceFailed : PhotoViewerEffect

    data object PhotoNotFound : PhotoViewerEffect
}
