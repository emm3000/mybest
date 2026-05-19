package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.MealType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private val MONTH_ABBR_ES = mapOf(
    1 to "ENE",
    2 to "FEB",
    3 to "MAR",
    4 to "ABR",
    5 to "MAY",
    6 to "JUN",
    7 to "JUL",
    8 to "AGO",
    9 to "SEP",
    10 to "OCT",
    11 to "NOV",
    12 to "DIC",
)

internal fun DayOfWeek.shortEs(): String = when (this) {
    DayOfWeek.MONDAY -> "LUN"
    DayOfWeek.TUESDAY -> "MAR"
    DayOfWeek.WEDNESDAY -> "MIE"
    DayOfWeek.THURSDAY -> "JUE"
    DayOfWeek.FRIDAY -> "VIE"
    DayOfWeek.SATURDAY -> "SAB"
    DayOfWeek.SUNDAY -> "DOM"
}

internal fun DayOfWeek.longEs(): String = when (this) {
    DayOfWeek.MONDAY -> "Lunes"
    DayOfWeek.TUESDAY -> "Martes"
    DayOfWeek.WEDNESDAY -> "Miércoles"
    DayOfWeek.THURSDAY -> "Jueves"
    DayOfWeek.FRIDAY -> "Viernes"
    DayOfWeek.SATURDAY -> "Sábado"
    DayOfWeek.SUNDAY -> "Domingo"
}

internal fun LocalDate.formatShortMonthDay(): String {
    val abbr = MONTH_ABBR_ES[month.ordinal + 1] ?: month.name.take(3)
    return "$day $abbr"
}

/**
 * Returns a compact topbar date string for the given date, e.g. "LUN 18 MAY".
 * HTopBar will uppercase the string itself via its letter-spacing style.
 */
internal fun formatTopbarDate(date: LocalDate, dow: DayOfWeek): String {
    val dayAbbr = dow.shortEs()
    val monthAbbr = MONTH_ABBR_ES[date.month.ordinal + 1] ?: date.month.name.take(3)
    return "$dayAbbr ${date.day} $monthAbbr"
}

internal fun MealType.labelEs(): String = when (this) {
    MealType.BREAKFAST -> "DESAYUNO"
    MealType.LUNCH -> "ALMUERZO"
    MealType.DINNER -> "CENA"
    MealType.SNACK -> "SNACK"
}
