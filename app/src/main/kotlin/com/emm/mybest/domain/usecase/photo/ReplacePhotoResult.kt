package com.emm.mybest.domain.usecase.photo

sealed interface ReplacePhotoResult {
    data object Replaced : ReplacePhotoResult

    data object PhotoNotFound : ReplacePhotoResult
}
