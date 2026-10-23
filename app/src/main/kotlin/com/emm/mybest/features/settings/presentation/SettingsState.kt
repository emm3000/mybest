package com.emm.mybest.features.settings.presentation

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val reminderPicker: ReminderField? = null,
    val appVersionLabel: String = "",
    val goals: SettingsGoals = SettingsGoals(),
)
