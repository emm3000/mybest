package com.emm.mybest.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DEFAULT_REMINDER_HOUR = intPreferencesKey("default_reminder_hour")
        val DEFAULT_REMINDER_MINUTE = intPreferencesKey("default_reminder_minute")
        val WEIGHT_REMINDER_HOUR = intPreferencesKey("weight_reminder_hour")
        val WEIGHT_REMINDER_MINUTE = intPreferencesKey("weight_reminder_minute")
    }

    override val isDarkMode: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DARK_MODE_ENABLED]
    }

    override suspend fun updateDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE_ENABLED] = enabled
        }
    }

    override val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override val defaultReminderTime: Flow<Pair<Int, Int>> = dataStore.data.map { preferences ->
        Pair(
            preferences[PreferencesKeys.DEFAULT_REMINDER_HOUR] ?: 20,
            preferences[PreferencesKeys.DEFAULT_REMINDER_MINUTE] ?: 0,
        )
    }

    override suspend fun updateDefaultReminderTime(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REMINDER_HOUR] = hour
            preferences[PreferencesKeys.DEFAULT_REMINDER_MINUTE] = minute
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
}
