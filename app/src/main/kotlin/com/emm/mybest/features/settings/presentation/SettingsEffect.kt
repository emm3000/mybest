package com.emm.mybest.features.settings.presentation

sealed class SettingsEffect {
    data class ShowError(val message: String) : SettingsEffect()
    data class ShowMessage(val message: String) : SettingsEffect()
    object GoalSaved : SettingsEffect()
    object GoalSaveFailed : SettingsEffect()
    object ReminderSaveFailed : SettingsEffect()
    object BackupRestored : SettingsEffect()
    object BackupFromOtherVersion : SettingsEffect()
    object BackupImportFailed : SettingsEffect()
    object BackupNotRestored : SettingsEffect()
}
