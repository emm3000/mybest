package com.emm.mybest.features.home.presentation

import com.emm.mybest.core.datetime.MONTH_ABBR_ES
import com.emm.mybest.core.datetime.shortEs
import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.models.PhotoType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

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

internal fun MealType.labelEs(): String = when (this) {
    MealType.BREAKFAST -> "DESAYUNO"
    MealType.LUNCH -> "ALMUERZO"
    MealType.DINNER -> "CENA"
    MealType.SNACK -> "SNACK"
}

internal fun DailySlot.labelEs(): String = when (this) {
    DailySlot.BREAKFAST -> "DESAYUNO"
    DailySlot.LUNCH -> "ALMUERZO"
    DailySlot.SNACK -> "SNACK"
    DailySlot.DINNER -> "CENA"
    DailySlot.EXERCISE -> "EJERCICIO"
}

internal fun LocalTime.formatHHmm(): String {
    val hh = hour.toString().padStart(2, '0')
    val mm = minute.toString().padStart(2, '0')
    return "$hh:$mm"
}

internal fun PhotoType.labelEs(): String = when (this) {
    PhotoType.TRUNK -> "Tronco"
    PhotoType.FACE -> "Cara"
}

internal fun formatHomeWeight(value: Float): String =
    "%.1f".format(java.util.Locale.getDefault(), value)
