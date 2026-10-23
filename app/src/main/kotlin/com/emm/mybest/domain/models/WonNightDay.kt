package com.emm.mybest.domain.models

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private val WON_NIGHT_DAYS = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)

/**
 * Whether a won night can be recorded on this app day: only Friday and Saturday evenings count.
 */
fun LocalDate.isWonNightDay(): Boolean = dayOfWeek in WON_NIGHT_DAYS
