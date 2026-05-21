package com.emm.mybest.features.settings.presentation

sealed class ReminderSettingsEffect {
    data class ShowError(val message: String) : ReminderSettingsEffect()
    data class ShowMessage(val message: String) : ReminderSettingsEffect()
}
