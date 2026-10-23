package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.Setup
import kotlinx.datetime.LocalTime

internal fun ReminderField.timeIn(setup: Setup): LocalTime? = when (this) {
    ReminderField.PRE_OUTING_TIME -> setup.preOutingReminderTime
    ReminderField.CHECK_IN_TIME -> setup.checkInReminderTime
    ReminderField.WEIGH_IN_TIME -> setup.weighInReminderTime
    ReminderField.DEBT_TIME -> setup.debtReminderTime
    ReminderField.WEIGH_IN_DAY -> null
}

internal fun ReminderField.isSetIn(setup: Setup): Boolean =
    if (this == ReminderField.WEIGH_IN_DAY) setup.weighInDay != null else timeIn(setup) != null

internal fun ReminderField.withTime(setup: Setup, time: LocalTime?): Setup = when (this) {
    ReminderField.PRE_OUTING_TIME -> setup.copy(preOutingReminderTime = time)
    ReminderField.CHECK_IN_TIME -> setup.copy(checkInReminderTime = time)
    ReminderField.WEIGH_IN_TIME -> setup.copy(weighInReminderTime = time)
    ReminderField.DEBT_TIME -> setup.copy(debtReminderTime = time)
    ReminderField.WEIGH_IN_DAY -> setup
}
