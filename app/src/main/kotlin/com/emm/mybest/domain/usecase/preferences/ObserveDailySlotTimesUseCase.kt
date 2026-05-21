package com.emm.mybest.domain.usecase.preferences

import com.emm.mybest.domain.models.DailySlotTimes
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObserveDailySlotTimesUseCase(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<DailySlotTimes> = repository.dailySlotTimes
}
