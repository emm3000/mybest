package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.photo.ReplacePhotoUseCase

data class HistoryPhotoUseCases(
    val delete: DeletePhotoUseCase,
    val replace: ReplacePhotoUseCase,
)
