package com.emm.mybest.core.datetime

import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class AppDayStartTest {

    @Test
    fun `the app day start is minute zero`() {
        assertEquals(0, LocalTime(5, 0).minutesIntoAppDay())
    }

    @Test
    fun `an evening time counts from the 05_00 start`() {
        assertEquals(1110, LocalTime(23, 30).minutesIntoAppDay())
    }

    @Test
    fun `a time past midnight wraps onto the same app day`() {
        assertEquals(1155, LocalTime(0, 15).minutesIntoAppDay())
    }

    @Test
    fun `the minute before the next start is the last minute of the app day`() {
        assertEquals(1439, LocalTime(4, 59).minutesIntoAppDay())
    }
}
