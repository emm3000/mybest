package com.emm.mybest.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isDarkMode: Flow<Boolean?>

    val notificationsEnabled: Flow<Boolean>

    suspend fun updateDarkMode(enabled: Boolean)

    suspend fun updateNotificationsEnabled(enabled: Boolean)
}
