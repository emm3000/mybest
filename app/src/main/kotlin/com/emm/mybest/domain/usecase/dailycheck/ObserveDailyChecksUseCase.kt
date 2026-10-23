package com.emm.mybest.domain.usecase.dailycheck

import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.repository.DailyCheckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class ObserveDailyChecksUseCase(
    private val repository: DailyCheckRepository,
) {

    /**
     * Emits the daily check types marked done on [appDay].
     */
    operator fun invoke(appDay: LocalDate): Flow<Set<DailyCheckType>> =
        repository.observeRange(appDay, appDay).map { checks ->
            checks.filter { it.isDone }.map { it.type }.toSet()
        }
}
