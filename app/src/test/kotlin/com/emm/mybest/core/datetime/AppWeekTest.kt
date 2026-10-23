package com.emm.mybest.core.datetime

import com.emm.mybest.testing.FixedClock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val MONDAY = LocalDate(2026, 10, 19)
private val SUNDAY = LocalDate(2026, 10, 25)

class AppWeekTest {

    @Test
    fun `a monday starts its own week`() {
        assertEquals(MONDAY..SUNDAY, appWeekOf(MONDAY))
    }

    @Test
    fun `a sunday ends its own week`() {
        assertEquals(MONDAY..SUNDAY, appWeekOf(SUNDAY))
    }

    @Test
    fun `saturday at 02_00 falls in the week of its app day`() {
        val saturdayTwoAm = LocalDateTime(2026, 10, 24, 2, 0).toInstant(LIMA)
        val clock = AppDayClock(FixedClock(saturdayTwoAm), LIMA)

        assertEquals(MONDAY..SUNDAY, appWeekOf(clock.appDayOf(saturdayTwoAm)))
    }

    @Test
    fun `monday at 02_00 falls in the previous week`() {
        val mondayTwoAm = LocalDateTime(2026, 10, 26, 2, 0).toInstant(LIMA)
        val clock = AppDayClock(FixedClock(mondayTwoAm), LIMA)

        assertEquals(MONDAY..SUNDAY, appWeekOf(clock.appDayOf(mondayTwoAm)))
    }
}
