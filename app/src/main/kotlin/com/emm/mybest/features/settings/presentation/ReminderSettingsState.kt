package com.emm.mybest.features.settings.presentation

import kotlinx.datetime.LocalTime

data class ReminderSettingsState(
    val notificationsEnabled: Boolean = true,
    val showDefaultTimePicker: Boolean = false,
    val weightReminderTime: LocalTime? = null,
)
