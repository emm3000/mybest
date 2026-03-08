package com.emm.mybest.core.datetime

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.lang.Math.floorDiv
import java.lang.Math.floorMod

private const val FIRST_MONTH = 1
private const val MONTHS_PER_YEAR = 12
private const val TWO_DIGIT_YEAR_DIVISOR = 100
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

fun currentDate(): LocalDate = Instant
    .fromEpochMilliseconds(System.currentTimeMillis())
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date

data class YearMonthValue(
    val year: Int,
    val month: Int,
) {
    init {
        require(month in FIRST_MONTH..MONTHS_PER_YEAR) { "Month must be in 1..12" }
    }

    fun atDay(dayOfMonth: Int): LocalDate = LocalDate(year, month, dayOfMonth)

    fun plusMonths(months: Int): YearMonthValue {
        val zeroBased = year * MONTHS_PER_YEAR + (month - FIRST_MONTH)
        val next = zeroBased + months
        return YearMonthValue(
            year = floorDiv(next, MONTHS_PER_YEAR),
            month = floorMod(next, MONTHS_PER_YEAR) + FIRST_MONTH,
        )
    }

    fun minusMonths(months: Int): YearMonthValue = plusMonths(-months)

    fun lengthOfMonth(): Int {
        val start = atDay(FIRST_MONTH).toEpochDays()
        val next = plusMonths(FIRST_MONTH).atDay(FIRST_MONTH).toEpochDays()
        return (next - start).toInt()
    }

    companion object {
        fun now(): YearMonthValue {
            val today = currentDate()
            return YearMonthValue(today.year, today.monthNumber)
        }

        fun from(date: LocalDate): YearMonthValue = YearMonthValue(date.year, date.monthNumber)
    }
}

fun LocalDate.minusDays(days: Int): LocalDate = this.plus(DatePeriod(days = -days))

fun DayOfWeek.shortEs(): String = when (this) {
    DayOfWeek.MONDAY -> "LUN"
    DayOfWeek.TUESDAY -> "MAR"
    DayOfWeek.WEDNESDAY -> "MIE"
    DayOfWeek.THURSDAY -> "JUE"
    DayOfWeek.FRIDAY -> "VIE"
    DayOfWeek.SATURDAY -> "SAB"
    DayOfWeek.SUNDAY -> "DOM"
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

fun LocalDate.formatDdMmYy(): String = "%02d/%02d/%02d".format(day, monthNumber, year % TWO_DIGIT_YEAR_DIVISOR)

fun LocalDate.formatEsLongDate(): String = "$day de ${monthNameEs(monthNumber)}, $year"

fun LocalDate.formatEsWeekdayDayMonth(): String {
    val weekday = dayNameEs(dayOfWeek).replaceFirstChar { it.uppercase() }
    return "$weekday $day ${monthNameEs(monthNumber)}"
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
