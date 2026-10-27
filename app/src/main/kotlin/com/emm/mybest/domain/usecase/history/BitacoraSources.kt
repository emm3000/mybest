package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.repository.NoteRepository
import com.emm.mybest.domain.usecase.relapse.ObserveReturnTimesUseCase

data class BitacoraSources(
    val body: HistoryBodySources,
    val nights: HistoryNightSources,
    val observeReturnTimes: ObserveReturnTimesUseCase,
    val money: BitacoraMoneySources,
    val noteRepository: NoteRepository,
    val appDayClock: AppDayClock,
)
