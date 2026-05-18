package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.reminder.WeightReminderScheduler
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.datetime.LocalTime

class UpdateDefaultReminderTimeUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val scheduler: WeightReminderScheduler,
) {
    suspend operator fun invoke(time: LocalTime?) {
        userPreferencesRepository.setReminderTime(time)
        if (time != null) {
            scheduler.schedule(time)
        } else {
            scheduler.cancel()
        }
    }
}
