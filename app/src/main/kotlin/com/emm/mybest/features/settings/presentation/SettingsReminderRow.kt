package com.emm.mybest.features.settings.presentation

import androidx.annotation.StringRes

data class SettingsReminderRow(
    val field: ReminderField,
    @StringRes val labelRes: Int,
    val value: SettingsRowValue,
    val isSet: Boolean,
)
