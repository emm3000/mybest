package com.emm.mybest.features.settings.presentation

sealed class SettingsIntent {
    data class OnNotificationsToggle(val enabled: Boolean) : SettingsIntent()
    data class OnExportBackup(val targetUri: String) : SettingsIntent()
    data class OnImportBackup(val sourceUri: String) : SettingsIntent()
    object OnDefaultTimePickerOpen : SettingsIntent()
    object OnDefaultTimePickerDismiss : SettingsIntent()
    data class OnDefaultReminderTimeChange(val hour: Int, val minute: Int) : SettingsIntent()
    object OnWeightReminderToggleOff : SettingsIntent()
}
