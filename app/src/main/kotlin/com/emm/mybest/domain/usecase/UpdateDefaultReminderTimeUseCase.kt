package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.repository.UserPreferencesRepository

class UpdateDefaultReminderTimeUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(hour: Int, minute: Int) {
        userPreferencesRepository.updateDefaultReminderTime(hour, minute)
    }
}
