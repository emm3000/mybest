package com.emm.mybest.core.datetime

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
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

    /**
     * The current instant of the clock this app day clock reads, so a caller can stamp a record and derive its
     * app day from the same moment.
     */
    fun now(): Instant = clock.now()

    fun today(): LocalDate = appDayOf(clock.now())

    fun appDayOf(instant: Instant): LocalDate {
        val local = instant.toLocalDateTime(timeZone)
        if (local.time < APP_DAY_START) return local.date.minus(1, DateTimeUnit.DAY)
        return local.date
    }

    fun localDateTimeOf(instant: Instant): LocalDateTime = instant.toLocalDateTime(timeZone)

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

    /**
     * Emits the local date and time now and again each time the clock reaches one of the local times in [at].
     */
    fun observeLocalDateTime(at: List<LocalTime>): Flow<LocalDateTime> = flow {
        emit(localDateTimeOf(clock.now()))
        while (awaitNextOccurrence(at)) emit(localDateTimeOf(clock.now()))
    }

    private suspend fun awaitNextOccurrence(times: List<LocalTime>): Boolean {
        val beforeDelay = clock.now()
        val next = nextOccurrence(times, beforeDelay) ?: return false
        delay((next - beforeDelay).coerceAtLeast(Duration.ZERO))
        return clock.now() != beforeDelay
    }

    private fun nextOccurrence(times: List<LocalTime>, after: Instant): Instant? {
        val date = localDateTimeOf(after).date
        return times
            .flatMap { time -> listOf(date, date.plus(1, DateTimeUnit.DAY)).map { LocalDateTime(it, time) } }
            .map { it.toInstant(timeZone) }
            .filter { it > after }
            .minOrNull()
    }
}
