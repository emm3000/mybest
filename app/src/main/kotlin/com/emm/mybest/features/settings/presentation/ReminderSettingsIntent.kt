package com.emm.mybest.features.settings.presentation

sealed class ReminderSettingsIntent {
    data class OnNotificationsToggle(val enabled: Boolean) : ReminderSettingsIntent()
    data class OnExportBackup(val targetUri: String) : ReminderSettingsIntent()
    data class OnImportBackup(val sourceUri: String) : ReminderSettingsIntent()
    object OnDefaultTimePickerOpen : ReminderSettingsIntent()
    object OnDefaultTimePickerDismiss : ReminderSettingsIntent()
    data class OnDefaultReminderTimeChange(val hour: Int, val minute: Int) : ReminderSettingsIntent()
    object OnWeightReminderToggleOff : ReminderSettingsIntent()
}
