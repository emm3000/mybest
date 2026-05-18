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
    return "$dayOfMonth $abbr"
}

internal fun MealType.labelEs(): String = when (this) {
    MealType.BREAKFAST -> "DESAYUNO"
    MealType.LUNCH -> "ALMUERZO"
    MealType.DINNER -> "CENA"
    MealType.SNACK -> "SNACK"
}
