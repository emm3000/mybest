package com.emm.mybest.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isDarkMode: Flow<Boolean?>

    val notificationsEnabled: Flow<Boolean>

    val defaultReminderTime: Flow<Pair<Int, Int>>

    suspend fun updateDarkMode(enabled: Boolean)

    suspend fun updateNotificationsEnabled(enabled: Boolean)

    suspend fun updateDefaultReminderTime(hour: Int, minute: Int)
}
