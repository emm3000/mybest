package com.emm.mybest.features.settings.presentation

import com.emm.mybest.R
import com.emm.mybest.core.datetime.formatHhMm
import com.emm.mybest.domain.models.Setup

private val LISTED_REMINDERS = listOf(
    ReminderField.PRE_OUTING_TIME,
    ReminderField.CHECK_IN_TIME,
    ReminderField.WEIGH_IN_DAY,
    ReminderField.WEIGH_IN_TIME,
)

internal fun Setup.toReminders(): SettingsReminders = SettingsReminders(
    rows = LISTED_REMINDERS.map { it.rowIn(this) },
    debtRow = ReminderField.DEBT_TIME.rowIn(this),
    isPhotoReminderEnabled = isPhotoReminderEnabled,
)

private fun ReminderField.rowIn(setup: Setup): SettingsReminderRow =
    SettingsReminderRow(field = this, labelRes = labelRes, value = valueIn(setup), isSet = isSetIn(setup))

private fun ReminderField.valueIn(setup: Setup): SettingsRowValue {
    val day = setup.weighInDay.takeIf { this == ReminderField.WEIGH_IN_DAY }
    val time = timeIn(setup)
    return when {
        day != null -> SettingsRowValue.Resource(day.weekdayRes)
        time != null -> SettingsRowValue.Text(time.formatHhMm())
        else -> SettingsRowValue.Resource(R.string.settings_goal_unset)
    }
}
