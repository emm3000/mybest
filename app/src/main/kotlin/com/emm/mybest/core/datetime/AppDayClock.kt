package com.emm.mybest.core.datetime

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Maps instants to app days: the 24 hours from 05:00 to 05:00 local time that every record belongs to.
 */
class AppDayClock(
    private val clock: Clock,
    private val timeZone: TimeZone,
) {

    fun today(): LocalDate = appDayOf(clock.now())

    fun appDayOf(instant: Instant): LocalDate {
        val local = instant.toLocalDateTime(timeZone)
        if (local.time < APP_DAY_START) return local.date.minus(1, DateTimeUnit.DAY)
        return local.date
    }

    fun startOf(appDay: LocalDate): Instant = LocalDateTime(appDay, APP_DAY_START).toInstant(timeZone)

    fun observeToday(): Flow<LocalDate> = flow {
        var current = today()
        emit(current)
        while (true) {
            val beforeDelay = clock.now()
            delay((startOf(current.plus(1, DateTimeUnit.DAY)) - beforeDelay).coerceAtLeast(Duration.ZERO))
            if (clock.now() == beforeDelay) break
            val next = today()
            if (next != current) {
                current = next
                emit(current)
            }
        }
    }
}
