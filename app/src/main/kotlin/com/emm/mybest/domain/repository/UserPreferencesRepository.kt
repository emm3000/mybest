package com.emm.mybest.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val notificationsEnabled: Flow<Boolean>

    suspend fun updateNotificationsEnabled(enabled: Boolean)
}
