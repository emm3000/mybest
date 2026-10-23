package com.emm.mybest.data.entities

import com.emm.mybest.core.datetime.YearMonthValue
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `a month is stored as a zero-padded year-month text`() {
        assertEquals("2026-03", converters.fromYearMonth(YearMonthValue(2026, 3)))
    }

    @Test
    fun `a stored month text reads back as the same month`() {
        assertEquals(YearMonthValue(2026, 11), converters.toYearMonth("2026-11"))
    }
}
