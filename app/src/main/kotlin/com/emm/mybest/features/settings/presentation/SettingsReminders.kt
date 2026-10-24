package com.emm.mybest.features.settings.presentation

data class SettingsReminders(
    val rows: List<SettingsReminderRow>,
    val debtRow: SettingsReminderRow,
    val isPhotoReminderEnabled: Boolean,
)
