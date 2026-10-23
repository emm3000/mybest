package com.emm.mybest.domain.usecase.dailycheck

import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.repository.DailyCheckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class ObserveDailyChecksUseCase(
    private val repository: DailyCheckRepository,
) {

    /**
     * Emits the daily checks recorded on [appDay], done or not; a check without a mark is absent.
     */
    operator fun invoke(appDay: LocalDate): Flow<List<DailyCheck>> = repository.observeRange(appDay, appDay)
}
