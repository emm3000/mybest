package com.emm.mybest.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.Hairline

private val CAPTURE_ROW_INSET = 28.dp

@Composable
internal fun SettingsCaptureRow(
    capture: SettingsCaptureState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsRow(
        params = SettingsRowParams(
            label = stringResource(R.string.settings_capture_label),
            value = captureValueText(capture),
            onValueClick = onClick,
        ),
        modifier = modifier,
    )
    Hairline(inset = CAPTURE_ROW_INSET)
}

@Composable
private fun captureValueText(capture: SettingsCaptureState): String {
    if (!capture.isActive) return stringResource(R.string.settings_capture_off)
    return pluralStringResource(R.plurals.settings_capture_active, capture.selectedAppCount, capture.selectedAppCount)
}
