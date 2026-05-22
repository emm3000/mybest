package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase

data class HomeMetricsUseCases(
    val observeWeightProgress: ObserveWeightProgressUseCase,
    val observePhotos: ObservePhotosUseCase,
)
