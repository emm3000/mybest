package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Decides whether the pre-outing reminder should notify now.
 */
class IsPreOutingReminderDueUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
) {

    /**
     * Whether notifications are turned on.
     */
    suspend operator fun invoke(): Boolean = userPreferencesRepository.notificationsEnabled.first()
}
