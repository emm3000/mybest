package com.emm.mybest.features.photo.presentation

sealed interface ComparePhotosIntent {
    data object Close : ComparePhotosIntent
}
