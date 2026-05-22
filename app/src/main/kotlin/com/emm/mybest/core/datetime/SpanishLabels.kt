package com.emm.mybest.core.datetime

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private const val FIRST_MONTH = 1
private const val MONTHS_PER_YEAR = 12

private val SPANISH_MONTHS = listOf(
    "enero",
    "febrero",
    "marzo",
    "abril",
    "mayo",
    "junio",
    "julio",
    "agosto",
    "septiembre",
    "octubre",
    "noviembre",
    "diciembre",
)

val MONTH_ABBR_ES = mapOf(
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

fun DayOfWeek.shortEs(): String = when (this) {
    DayOfWeek.MONDAY -> "LUN"
    DayOfWeek.TUESDAY -> "MAR"
    DayOfWeek.WEDNESDAY -> "MIÉ"
    DayOfWeek.THURSDAY -> "JUE"
    DayOfWeek.FRIDAY -> "VIE"
    DayOfWeek.SATURDAY -> "SÁB"
    DayOfWeek.SUNDAY -> "DOM"
}

fun DayOfWeek.longEs(): String = when (this) {
    DayOfWeek.MONDAY -> "Lunes"
    DayOfWeek.TUESDAY -> "Martes"
    DayOfWeek.WEDNESDAY -> "Miércoles"
    DayOfWeek.THURSDAY -> "Jueves"
    DayOfWeek.FRIDAY -> "Viernes"
    DayOfWeek.SATURDAY -> "Sábado"
    DayOfWeek.SUNDAY -> "Domingo"
}

fun DayOfWeek.narrowEs(): String = when (this) {
    DayOfWeek.MONDAY -> "L"
    DayOfWeek.TUESDAY -> "M"
    DayOfWeek.WEDNESDAY -> "X"
    DayOfWeek.THURSDAY -> "J"
    DayOfWeek.FRIDAY -> "V"
    DayOfWeek.SATURDAY -> "S"
    DayOfWeek.SUNDAY -> "D"
}

fun LocalDate.formatEsLongDate(): String = "$day de ${monthNameEs(month.ordinal + 1)}, $year"

fun LocalDate.formatEsWeekdayDayMonth(): String {
    val weekday = dayNameEs(dayOfWeek).replaceFirstChar { it.uppercase() }
    return "$weekday $day ${monthNameEs(month.ordinal + 1)}"
}

fun YearMonthValue.formatEsMonthYear(): String {
    val value = "${monthNameEs(month)} $year"
    return value.replaceFirstChar { it.uppercase() }
}

private fun monthNameEs(month: Int): String = when (month) {
    in FIRST_MONTH..MONTHS_PER_YEAR -> SPANISH_MONTHS[month - FIRST_MONTH]
    else -> error("Invalid month: $month")
}

private fun dayNameEs(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "lunes"
    DayOfWeek.TUESDAY -> "martes"
    DayOfWeek.WEDNESDAY -> "miercoles"
    DayOfWeek.THURSDAY -> "jueves"
    DayOfWeek.FRIDAY -> "viernes"
    DayOfWeek.SATURDAY -> "sabado"
    DayOfWeek.SUNDAY -> "domingo"
}
