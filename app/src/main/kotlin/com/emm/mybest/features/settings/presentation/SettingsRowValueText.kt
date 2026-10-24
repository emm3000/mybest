package com.emm.mybest.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

@Composable
internal fun SettingsRowValue.resolve(): String = when (this) {
    is SettingsRowValue.Text -> text
    is SettingsRowValue.Resource -> if (arg == null) stringResource(res) else stringResource(res, arg)
    is SettingsRowValue.Plural -> pluralStringResource(res, count, count)
}
