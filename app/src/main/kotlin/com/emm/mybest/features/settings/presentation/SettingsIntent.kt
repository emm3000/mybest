package com.emm.mybest.features.settings.presentation

import kotlinx.datetime.DayOfWeek

sealed class SettingsIntent {
    data class OnNotificationsToggle(val enabled: Boolean) : SettingsIntent()
    sealed class BackupIntent : SettingsIntent()
    data class OnExportBackup(val targetUri: String) : BackupIntent()
    data class OnImportBackup(val sourceUri: String) : BackupIntent()

    sealed class FieldIntent : SettingsIntent()
    data class OnFieldOpen(val field: SettingsField) : FieldIntent()
    data class OnFieldInputChange(val input: String) : FieldIntent()
    object OnFieldSave : FieldIntent()
    object OnFieldClear : FieldIntent()
    object OnFieldDismiss : FieldIntent()

    sealed class ReminderIntent : SettingsIntent()
    data class OnReminderOpen(val field: ReminderField) : ReminderIntent()
    object OnReminderDismiss : ReminderIntent()
    data class OnReminderTimeChange(val hour: Int, val minute: Int) : ReminderIntent()
    data class OnWeighInDayChange(val day: DayOfWeek) : ReminderIntent()
    data class OnReminderClear(val field: ReminderField) : ReminderIntent()
    data class OnPhotoReminderToggle(val isEnabled: Boolean) : ReminderIntent()
}
