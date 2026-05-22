package com.emm.mybest.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.DailySlotTimes
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    constructor(context: Context) : this(context.dataStore)

    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val WEIGHT_REMINDER_HOUR = intPreferencesKey("weight_reminder_hour")
        val WEIGHT_REMINDER_MINUTE = intPreferencesKey("weight_reminder_minute")
        val SLOT_TIME_BREAKFAST_MIN = intPreferencesKey("slot_time_breakfast_min")
        val SLOT_TIME_LUNCH_MIN = intPreferencesKey("slot_time_lunch_min")
        val SLOT_TIME_SNACK_MIN = intPreferencesKey("slot_time_snack_min")
        val SLOT_TIME_DINNER_MIN = intPreferencesKey("slot_time_dinner_min")
        val SLOT_TIME_EXERCISE_MIN = intPreferencesKey("slot_time_exercise_min")
    }

    private fun DailySlot.prefKey() = when (this) {
        DailySlot.BREAKFAST -> PreferencesKeys.SLOT_TIME_BREAKFAST_MIN
        DailySlot.LUNCH -> PreferencesKeys.SLOT_TIME_LUNCH_MIN
        DailySlot.SNACK -> PreferencesKeys.SLOT_TIME_SNACK_MIN
        DailySlot.DINNER -> PreferencesKeys.SLOT_TIME_DINNER_MIN
        DailySlot.EXERCISE -> PreferencesKeys.SLOT_TIME_EXERCISE_MIN
    }

    override val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override val weightReminderTime: Flow<LocalTime?> = dataStore.data.map { preferences ->
        val hour = preferences[PreferencesKeys.WEIGHT_REMINDER_HOUR]
        val minute = preferences[PreferencesKeys.WEIGHT_REMINDER_MINUTE]
        if (hour != null && minute != null) LocalTime(hour, minute) else null
    }

    override suspend fun setReminderTime(time: LocalTime?) {
        dataStore.edit { preferences ->
            if (time != null) {
                preferences[PreferencesKeys.WEIGHT_REMINDER_HOUR] = time.hour
                preferences[PreferencesKeys.WEIGHT_REMINDER_MINUTE] = time.minute
            } else {
                preferences.remove(PreferencesKeys.WEIGHT_REMINDER_HOUR)
                preferences.remove(PreferencesKeys.WEIGHT_REMINDER_MINUTE)
            }
        }
    }

    override val dailySlotTimes: Flow<DailySlotTimes> = dataStore.data.map { preferences ->
        val resolved = DailySlot.entries.associateWith { slot ->
            preferences[slot.prefKey()]
                ?.let { minutesSinceMidnight ->
                    LocalTime(
                        hour = minutesSinceMidnight / MINUTES_PER_HOUR,
                        minute = minutesSinceMidnight % MINUTES_PER_HOUR,
                    )
                }
                ?: DailySlotTimes.DEFAULT_TIMES.getValue(slot)
        }
        DailySlotTimes(resolved)
    }

    override suspend fun setDailySlotTime(slot: DailySlot, time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[slot.prefKey()] = time.hour * MINUTES_PER_HOUR + time.minute
        }
    }

    private companion object {
        const val MINUTES_PER_HOUR = 60
    }
}
