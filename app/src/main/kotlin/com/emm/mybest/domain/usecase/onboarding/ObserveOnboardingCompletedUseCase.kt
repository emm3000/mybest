package com.emm.mybest.domain.usecase.onboarding

import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObserveOnboardingCompletedUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = userPreferencesRepository.onboardingCompleted
}
