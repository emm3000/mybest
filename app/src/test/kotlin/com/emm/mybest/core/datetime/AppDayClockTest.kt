package com.emm.mybest.core.datetime

import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.VirtualTimeClock
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val LIMA = TimeZone.of("America/Lima")
private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)
private val NOON = LocalTime(12, 0)
private val EVENING = LocalTime(18, 0)

private fun lima(date: LocalDate, hour: Int, minute: Int = 0): Instant =
    LocalDateTime(date.year, date.month, date.day, hour, minute).toInstant(LIMA)

private class EarlyWakeClock(private val start: Instant, private val scope: TestScope) : Clock {
    override fun now(): Instant {
        val elapsed = scope.testScheduler.currentTime.milliseconds
        if (elapsed == Duration.ZERO) return start
        return start + elapsed - 1.seconds
    }
}

class AppDayClockTest {

    private fun clockAt(instant: Instant) = AppDayClock(FixedClock(instant), LIMA)

    @Test
    fun `saturday at 02_00 belongs to friday`() {
        assertEquals(FRIDAY, clockAt(lima(SATURDAY, 2)).today())
    }

    @Test
    fun `04_59 belongs to the previous date`() {
        assertEquals(FRIDAY, clockAt(lima(SATURDAY, 4, 59)).today())
    }

    @Test
    fun `05_00 starts a new app day`() {
        assertEquals(SATURDAY, clockAt(lima(SATURDAY, 5)).today())
    }

    @Test
    fun `23_59 belongs to the same date`() {
        assertEquals(SATURDAY, clockAt(lima(SATURDAY, 23, 59)).today())
    }

    @Test
    fun `appDayOf maps an instant with the cutoff`() {
        val clock = clockAt(lima(FRIDAY, 12))

        assertEquals(FRIDAY, clock.appDayOf(lima(SATURDAY, 2)))
        assertEquals(SATURDAY, clock.appDayOf(lima(SATURDAY, 5)))
    }

    @Test
    fun `localDateTimeOf shows an instant in the clock zone without the cutoff`() {
        val clock = clockAt(lima(FRIDAY, 12))

        assertEquals(LocalDateTime(2026, 10, 24, 2, 30), clock.localDateTimeOf(lima(SATURDAY, 2, 30)))
    }

    @Test
    fun `startOf an app day is that date at 05_00 local`() {
        val clock = clockAt(lima(FRIDAY, 12))

        assertEquals(lima(SATURDAY, 5), clock.startOf(SATURDAY))
    }

    @Test
    fun `observeToday emits the next app day after the 05_00 boundary`() = runTest {
        val clock = AppDayClock(VirtualTimeClock(lima(FRIDAY, 23), this), LIMA)

        val days = clock.observeToday().take(2).toList()

        assertEquals(listOf(FRIDAY, SATURDAY), days)
    }

    @Test
    fun `observeToday keeps waiting when it wakes before the 05_00 boundary`() = runTest {
        val clock = AppDayClock(EarlyWakeClock(lima(FRIDAY, 23), this), LIMA)

        val days = clock.observeToday().take(2).toList()

        assertEquals(listOf(FRIDAY, SATURDAY), days)
    }

    @Test
    fun `observeToday completes when the clock did not advance`() = runTest {
        val days = clockAt(lima(FRIDAY, 23)).observeToday().toList()

        assertEquals(listOf(FRIDAY), days)
    }

    @Test
    fun `observeLocalDateTime emits at each listed time`() = runTest {
        val clock = AppDayClock(VirtualTimeClock(lima(FRIDAY, 11, 30), this), LIMA)

        val times = clock.observeLocalDateTime(listOf(NOON, EVENING)).take(3).toList()

        assertEquals(
            listOf(
                LocalDateTime(2026, 10, 23, 11, 30),
                LocalDateTime(2026, 10, 23, 12, 0),
                LocalDateTime(2026, 10, 23, 18, 0),
            ),
            times,
        )
    }

    @Test
    fun `observeLocalDateTime continues on the next day after the last time`() = runTest {
        val clock = AppDayClock(VirtualTimeClock(lima(FRIDAY, 19), this), LIMA)

        val times = clock.observeLocalDateTime(listOf(NOON, EVENING)).take(2).toList()

        assertEquals(LocalDateTime(2026, 10, 24, 12, 0), times.last())
    }

    @Test
    fun `observeLocalDateTime completes when the clock did not advance`() = runTest {
        val times = clockAt(lima(FRIDAY, 11)).observeLocalDateTime(listOf(NOON)).toList()

        assertEquals(listOf(LocalDateTime(2026, 10, 23, 11, 0)), times)
    }
}
