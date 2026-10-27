package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.repository.NoteRepository
import com.emm.mybest.domain.usecase.relapse.ObserveReturnTimesUseCase

data class LogbookSources(
    val body: HistoryBodySources,
    val nights: HistoryNightSources,
    val observeReturnTimes: ObserveReturnTimesUseCase,
    val money: LogbookMoneySources,
    val noteRepository: NoteRepository,
    val appDayClock: AppDayClock,
)
