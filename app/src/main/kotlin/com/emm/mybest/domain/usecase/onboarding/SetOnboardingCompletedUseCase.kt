package com.emm.mybest.domain.usecase.onboarding

import com.emm.mybest.domain.repository.UserPreferencesRepository

class SetOnboardingCompletedUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(completed: Boolean) {
        userPreferencesRepository.updateOnboardingCompleted(completed)
    }
}
