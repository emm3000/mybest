package com.emm.mybest.features.settings.presentation

import androidx.annotation.StringRes

data class SettingsGoalRow(
    val field: SettingsField,
    @StringRes val labelRes: Int,
    val value: SettingsRowValue,
)
