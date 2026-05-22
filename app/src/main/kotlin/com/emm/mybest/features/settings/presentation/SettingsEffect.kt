package com.emm.mybest.features.settings.presentation

sealed class SettingsEffect {
    data class ShowError(val message: String) : SettingsEffect()
    data class ShowMessage(val message: String) : SettingsEffect()
}
