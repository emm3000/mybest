package com.emm.mybest.features.home.presentation

import com.emm.mybest.core.datetime.MONTH_ABBR_ES
import com.emm.mybest.core.datetime.shortEs
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

internal fun LocalDate.formatShortMonthDay(): String {
    val abbr = MONTH_ABBR_ES[month.ordinal + 1] ?: month.name.take(3)
    return "$day $abbr"
}

/** Returns a compact topbar date string for the given date, e.g. "LUN 18 MAY". */
internal fun formatTopbarDate(date: LocalDate, dow: DayOfWeek): String {
    val dayAbbr = dow.shortEs()
    val monthAbbr = MONTH_ABBR_ES[date.month.ordinal + 1] ?: date.month.name.take(3)
    return "$dayAbbr ${date.day} $monthAbbr"
}

/** Returns "MIÉ · 21 MAY" format for the Home header row. */
internal fun formatHomeHeaderDate(date: LocalDate, dow: DayOfWeek): String {
    val dayAbbr = dow.shortEs()
    val monthAbbr = MONTH_ABBR_ES[date.month.ordinal + 1] ?: date.month.name.take(3)
    return "$dayAbbr · ${date.day} $monthAbbr"
}

internal fun formatHomeWeight(value: Float): String =
    "%.1f".format(java.util.Locale.getDefault(), value)
