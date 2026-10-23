package com.emm.mybest.domain.usecase.history

import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository

data class HistoryBodySources(
    val weightRepository: WeightRepository,
    val photoRepository: PhotoRepository,
)
