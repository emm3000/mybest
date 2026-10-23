package com.emm.mybest.domain.reminder

import com.emm.mybest.testing.FixedClock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.hours

private val LIMA = TimeZone.of("America/Lima")

class ReminderDelaysTest {

    @Test
    fun `waits until the next occurrence in the local time zone`() {
        val friday6pm = LocalDateTime(2026, 10, 23, 18, 0).toInstant(LIMA)
        val delays = ReminderDelays(FixedClock(friday6pm), LIMA)

        val delay = delays.untilNext(setOf(DayOfWeek.FRIDAY), LocalTime(19, 0))

        assertEquals(1.hours, delay)
    }
}
