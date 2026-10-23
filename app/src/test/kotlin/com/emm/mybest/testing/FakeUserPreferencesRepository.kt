package com.emm.mybest.testing

import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository(isNotificationsEnabled: Boolean = true) : UserPreferencesRepository {
    private val enabled = MutableStateFlow(isNotificationsEnabled)

    override val notificationsEnabled: Flow<Boolean> = enabled

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        this.enabled.value = enabled
    }
}
