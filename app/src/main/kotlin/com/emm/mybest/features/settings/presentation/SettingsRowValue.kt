package com.emm.mybest.features.settings.presentation

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

sealed interface SettingsRowValue {
    data class Text(val text: String) : SettingsRowValue

    data class Resource(
        @StringRes val res: Int,
        val arg: String? = null,
    ) : SettingsRowValue

    data class Plural(
        @PluralsRes val res: Int,
        val count: Int,
    ) : SettingsRowValue
}
