package com.emm.mybest.core.datetime

import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeLabelsTest {

    @Test
    fun `formats hours and minutes with two digits`() {
        assertEquals("01:05", LocalTime(1, 5).formatHhMm())
        assertEquals("23:30", LocalTime(23, 30).formatHhMm())
    }
}
