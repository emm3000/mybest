package com.emm.mybest.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isDarkMode: Flow<Boolean?>
    val notificationsEnabled: Flow<Boolean>
    val useDynamicColor: Flow<Boolean>

    suspend fun updateDarkMode(enabled: Boolean)
    suspend fun updateDynamicColor(enabled: Boolean)
}
