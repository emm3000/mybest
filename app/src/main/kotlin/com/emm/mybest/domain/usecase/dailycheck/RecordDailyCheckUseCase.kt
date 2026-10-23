package com.emm.mybest.domain.usecase.dailycheck

import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.repository.DailyCheckRepository
import kotlinx.datetime.LocalDate

class RecordDailyCheckUseCase(
    private val repository: DailyCheckRepository,
) {

    /**
     * Records the [type] check of [appDay] as [isDone], replacing any earlier mark.
     */
    suspend operator fun invoke(appDay: LocalDate, type: DailyCheckType, isDone: Boolean) {
        repository.setDone(appDay, type, isDone)
    }
}
