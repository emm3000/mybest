package com.emm.mybest.features.settings.presentation

import kotlinx.datetime.LocalTime

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val showDefaultTimePicker: Boolean = false,
    val weightReminderTime: LocalTime? = null,
    val appVersionLabel: String = "",
)
