package com.emm.mybest.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.HSwitch
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone

private val SECTION_GUT = 28.dp
private val SECTION_TOP = 24.dp
private val SWITCH_ROW_VERTICAL_PADDING = 22.dp

@Composable
internal fun SettingsRemindersSection(
    reminders: SettingsReminders,
    onIntent: (SettingsIntent.ReminderIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MicroLabel(
            text = stringResource(R.string.settings_reminders_section),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
            modifier = Modifier.padding(start = SECTION_GUT, top = SECTION_TOP),
        )
        reminders.rows.forEach { row -> ReminderRow(row = row, onIntent = onIntent) }
        PhotoReminderRow(
            isEnabled = reminders.isPhotoReminderEnabled,
            onToggle = { onIntent(SettingsIntent.OnPhotoReminderToggle(it)) },
        )
        Hairline(inset = SECTION_GUT)
        ReminderRow(row = reminders.debtRow, onIntent = onIntent)
    }
}

@Composable
private fun ReminderRow(
    row: SettingsReminderRow,
    onIntent: (SettingsIntent.ReminderIntent) -> Unit,
) {
    SettingsRow(
        params = SettingsRowParams(
            label = stringResource(row.labelRes),
            value = row.value.resolve(),
            extra = if (row.isSet) stringResource(R.string.settings_goal_clear) else null,
            onValueClick = { onIntent(SettingsIntent.OnReminderOpen(row.field)) },
            onExtraClick = { onIntent(SettingsIntent.OnReminderClear(row.field)) },
        ),
    )
    Hairline(inset = SECTION_GUT)
}

@Composable
private fun PhotoReminderRow(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val label = stringResource(R.string.settings_reminder_photo_label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SECTION_GUT, vertical = SWITCH_ROW_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicroLabel(
            text = label,
            modifier = Modifier.weight(1f),
            style = MicroLabelStyle(tone = if (isEnabled) MicroLabelTone.Done else MicroLabelTone.Default),
        )
        HSwitch(checked = isEnabled, onCheckedChange = onToggle, contentDescription = label)
    }
}
