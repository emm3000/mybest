package com.emm.mybest.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime

interface UserPreferencesRepository {
    val isDarkMode: Flow<Boolean?>

    val notificationsEnabled: Flow<Boolean>

    val defaultReminderTime: Flow<Pair<Int, Int>>

    /** Null means the user has not configured a weight reminder yet (no scheduling). */
    val weightReminderTime: Flow<LocalTime?>

    suspend fun updateDarkMode(enabled: Boolean)

    suspend fun updateNotificationsEnabled(enabled: Boolean)

    suspend fun updateDefaultReminderTime(hour: Int, minute: Int)

    /** Persists [time] as the weight reminder time. Null clears it (disables reminder). */
    suspend fun setReminderTime(time: LocalTime?)
}
