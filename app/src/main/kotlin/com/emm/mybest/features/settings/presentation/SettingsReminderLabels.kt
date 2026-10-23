package com.emm.mybest.features.settings.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R
import kotlinx.datetime.DayOfWeek

@get:StringRes
internal val ReminderField.labelRes: Int
    get() = when (this) {
        ReminderField.PRE_OUTING_TIME -> R.string.settings_reminder_pre_outing_label
        ReminderField.CHECK_IN_TIME -> R.string.settings_reminder_check_in_label
        ReminderField.WEIGH_IN_DAY -> R.string.settings_reminder_weigh_in_day_label
        ReminderField.WEIGH_IN_TIME -> R.string.settings_reminder_weigh_in_time_label
        ReminderField.DEBT_TIME -> R.string.settings_reminder_debt_label
    }

@get:StringRes
internal val DayOfWeek.weekdayRes: Int
    get() = when (this) {
        DayOfWeek.MONDAY -> R.string.settings_weekday_monday
        DayOfWeek.TUESDAY -> R.string.settings_weekday_tuesday
        DayOfWeek.WEDNESDAY -> R.string.settings_weekday_wednesday
        DayOfWeek.THURSDAY -> R.string.settings_weekday_thursday
        DayOfWeek.FRIDAY -> R.string.settings_weekday_friday
        DayOfWeek.SATURDAY -> R.string.settings_weekday_saturday
        DayOfWeek.SUNDAY -> R.string.settings_weekday_sunday
    }
