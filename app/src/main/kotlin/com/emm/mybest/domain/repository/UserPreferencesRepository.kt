package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.DailySlotTimes
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime

interface UserPreferencesRepository {
    val isDarkMode: Flow<Boolean?>

    val notificationsEnabled: Flow<Boolean>

    /** Null means the user has not configured a weight reminder yet (no scheduling). */
    val weightReminderTime: Flow<LocalTime?>

    /** Times shown on Home for each daily slot. Defaults from [DailySlotTimes.DEFAULT_TIMES]. */
    val dailySlotTimes: Flow<DailySlotTimes>

    suspend fun updateDarkMode(enabled: Boolean)

    suspend fun updateNotificationsEnabled(enabled: Boolean)

    /** Persists [time] as the weight reminder time. Null clears it (disables reminder). */
    suspend fun setReminderTime(time: LocalTime?)

    /** Overrides the configured time for [slot]. Applies globally (every day). */
    suspend fun setDailySlotTime(slot: DailySlot, time: LocalTime)
}
