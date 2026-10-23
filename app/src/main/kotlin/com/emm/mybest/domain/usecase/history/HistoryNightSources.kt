package com.emm.mybest.domain.usecase.history

import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.domain.usecase.relapse.ObserveRelapsesUseCase

data class HistoryNightSources(
    val outingRepository: OutingRepository,
    val observeRelapses: ObserveRelapsesUseCase,
)
