package com.emm.mybest.features.photo.presentation

import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto

sealed class ComparePhotosIntent {
    data class OnTypeSelected(val type: PhotoType?) : ComparePhotosIntent()
    data class OnBeforePhotoSelected(val photo: ProgressPhoto) : ComparePhotosIntent()
    data class OnAfterPhotoSelected(val photo: ProgressPhoto) : ComparePhotosIntent()
    object ToggleSwap : ComparePhotosIntent()
}
