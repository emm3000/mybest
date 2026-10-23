package com.emm.mybest.testing

import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository(
    isNotificationsEnabled: Boolean = true,
    isOnboardingCompleted: Boolean = false,
) : UserPreferencesRepository {
    private val enabled = MutableStateFlow(isNotificationsEnabled)
    private val completed = MutableStateFlow(isOnboardingCompleted)

    override val notificationsEnabled: Flow<Boolean> = enabled

    override val onboardingCompleted: Flow<Boolean> = completed

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        this.enabled.value = enabled
    }

    override suspend fun updateOnboardingCompleted(completed: Boolean) {
        this.completed.value = completed
    }
}
