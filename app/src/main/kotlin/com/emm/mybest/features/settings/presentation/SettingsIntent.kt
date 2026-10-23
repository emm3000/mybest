package com.emm.mybest.features.settings.presentation

sealed class SettingsIntent {
    data class OnNotificationsToggle(val enabled: Boolean) : SettingsIntent()
    sealed class BackupIntent : SettingsIntent()
    data class OnExportBackup(val targetUri: String) : BackupIntent()
    data class OnImportBackup(val sourceUri: String) : BackupIntent()
    object OnDefaultTimePickerOpen : SettingsIntent()
    object OnDefaultTimePickerDismiss : SettingsIntent()
    data class OnDefaultReminderTimeChange(val hour: Int, val minute: Int) : SettingsIntent()
    object OnWeightReminderToggleOff : SettingsIntent()

    sealed class FieldIntent : SettingsIntent()
    data class OnFieldOpen(val field: SettingsField) : FieldIntent()
    data class OnFieldInputChange(val input: String) : FieldIntent()
    object OnFieldSave : FieldIntent()
    object OnFieldClear : FieldIntent()
    object OnFieldDismiss : FieldIntent()
}
