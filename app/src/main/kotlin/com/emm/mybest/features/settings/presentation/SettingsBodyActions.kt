package com.emm.mybest.features.settings.presentation

internal data class SettingsBodyActions(
    val onReminderIntent: (SettingsIntent.ReminderIntent) -> Unit,
    val onFieldOpen: (SettingsField) -> Unit,
    val onDebtsClick: () -> Unit,
    val onExportClick: () -> Unit,
    val onImportClick: () -> Unit,
)
