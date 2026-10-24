package com.emm.mybest.features.settings.presentation

import androidx.compose.runtime.Composable
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.ui.components.ReminderTimePickerDialog
import kotlinx.datetime.LocalTime

private const val PICKER_FALLBACK_HOUR = 8
private const val PICKER_FALLBACK_MINUTE = 0

@Composable
internal fun SettingsReminderPicker(
    field: ReminderField,
    setup: Setup,
    onIntent: (SettingsIntent.ReminderIntent) -> Unit,
) {
    if (field == ReminderField.WEIGH_IN_DAY) {
        SettingsWeighInDayPicker(selected = setup.weighInDay, onIntent = onIntent)
    } else {
        val current = field.timeIn(setup)
        ReminderTimePickerDialog(
            initialTime = current ?: LocalTime(PICKER_FALLBACK_HOUR, PICKER_FALLBACK_MINUTE),
            onConfirm = { hour, minute -> onIntent(SettingsIntent.OnReminderTimeChange(hour, minute)) },
            onDismiss = { onIntent(SettingsIntent.OnReminderDismiss) },
        )
    }
}
