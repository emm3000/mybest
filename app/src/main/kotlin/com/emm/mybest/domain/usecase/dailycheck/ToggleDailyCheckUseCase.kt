package com.emm.mybest.domain.usecase.dailycheck

import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.repository.DailyCheckRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

class ToggleDailyCheckUseCase(
    private val repository: DailyCheckRepository,
) {

    /**
     * Flips the [type] check of [appDay]; an unrecorded check becomes done.
     */
    suspend operator fun invoke(appDay: LocalDate, type: DailyCheckType) {
        val isDone = repository.observeRange(appDay, appDay).first()
            .any { it.type == type && it.isDone }
        repository.setDone(appDay, type, !isDone)
    }
}
