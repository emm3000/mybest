package com.emm.mybest.features.settings.presentation

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val showDefaultTimePicker: Boolean = false,
    val appVersionLabel: String = "",
    val goals: SettingsGoals = SettingsGoals(),
)
