package com.emm.mybest.features.history.presentation

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.datetime.shortEs
import kotlinx.datetime.LocalDate

private val SPANISH_MONTHS_UPPER = listOf(
    "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
    "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE",
)

private val SPANISH_MONTHS_CAPITALIZED = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)

private val MONTH_SHORT_UPPER = listOf(
    "ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
    "JUL", "AGO", "SEP", "OCT", "NOV", "DIC",
)

fun formatMonthLabel(month: YearMonthValue): String {
    val monthName = SPANISH_MONTHS_UPPER.getOrElse(month.month - 1) { "?" }
    return "$monthName · ${month.year}"
}

fun formatMonthName(month: YearMonthValue): String =
    SPANISH_MONTHS_CAPITALIZED.getOrElse(month.month - 1) { "?" }

fun formatRecentDate(date: LocalDate): String {
    val day = date.day.toString().padStart(2, '0')
    val monthIndex = date.month.ordinal
    val monthShort = MONTH_SHORT_UPPER.getOrElse(monthIndex) { "?" }
    val dow = date.dayOfWeek.shortEs()
    return "$day $monthShort · $dow"
}
