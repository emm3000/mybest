package com.emm.mybest.core.datetime

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

class DateTimeUtilsTest {

    @Test
    fun `currentDate returns a valid date`() {
        val today = currentDate()

        assertNotNull(today)
    }

    @Test
    fun `YearMonthValue validates month range`() {
        assertThrows(IllegalArgumentException::class.java) {
            YearMonthValue(year = 2026, month = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            YearMonthValue(year = 2026, month = 13)
        }
    }

    @Test
    fun `YearMonthValue from and atDay create expected date`() {
        val yearMonth = YearMonthValue.from(LocalDate(2026, 3, 8))

        assertEquals(2026, yearMonth.year)
        assertEquals(3, yearMonth.month)
        assertEquals(LocalDate(2026, 3, 10), yearMonth.atDay(10))
    }

    @Test
    fun `YearMonthValue plusMonths and minusMonths handle boundaries`() {
        val value = YearMonthValue(year = 2026, month = 1)

        assertEquals(YearMonthValue(2026, 4), value.plusMonths(3))
        assertEquals(YearMonthValue(2025, 12), value.minusMonths(1))
        assertEquals(YearMonthValue(2025, 11), value.plusMonths(-2))
    }

    @Test
    fun `YearMonthValue lengthOfMonth handles leap and non leap months`() {
        val leapFebruary = YearMonthValue(year = 2024, month = 2)
        val normalFebruary = YearMonthValue(year = 2025, month = 2)

        assertEquals(29, leapFebruary.lengthOfMonth())
        assertEquals(28, normalFebruary.lengthOfMonth())
    }

    @Test
    fun `minusDays subtracts expected amount`() {
        val date = LocalDate(2026, 3, 8)

        assertEquals(LocalDate(2026, 3, 3), date.minusDays(5))
    }

    @Test
    fun `day of week helpers return expected spanish labels`() {
        assertEquals("LUN", DayOfWeek.MONDAY.shortEs())
        assertEquals("DOM", DayOfWeek.SUNDAY.shortEs())
        assertEquals("M", DayOfWeek.TUESDAY.narrowEs())
        assertEquals("D", DayOfWeek.SUNDAY.narrowEs())
    }

    @Test
    fun `shortEs and narrowEs map all weekdays`() {
        val shortExpected = mapOf(
            DayOfWeek.MONDAY to "LUN",
            DayOfWeek.TUESDAY to "MAR",
            DayOfWeek.WEDNESDAY to "MIE",
            DayOfWeek.THURSDAY to "JUE",
            DayOfWeek.FRIDAY to "VIE",
            DayOfWeek.SATURDAY to "SAB",
            DayOfWeek.SUNDAY to "DOM",
        )
        val narrowExpected = mapOf(
            DayOfWeek.MONDAY to "L",
            DayOfWeek.TUESDAY to "M",
            DayOfWeek.WEDNESDAY to "X",
            DayOfWeek.THURSDAY to "J",
            DayOfWeek.FRIDAY to "V",
            DayOfWeek.SATURDAY to "S",
            DayOfWeek.SUNDAY to "D",
        )

        DayOfWeek.entries.forEach { day ->
            assertEquals(shortExpected[day], day.shortEs())
            assertEquals(narrowExpected[day], day.narrowEs())
        }
    }

    @Test
    fun `date format helpers return expected spanish formatting`() {
        val date = LocalDate(2026, 3, 8)
        val yearMonth = YearMonthValue(year = 2026, month = 3)

        assertEquals("08/03/26", date.formatDdMmYy())
        assertEquals("8 de marzo, 2026", date.formatEsLongDate())
        assertEquals("Domingo 8 marzo", date.formatEsWeekdayDayMonth())
        assertEquals("Marzo 2026", yearMonth.formatEsMonthYear())
    }

    @Test
    fun `formatEsWeekdayDayMonth returns expected weekday names for full week`() {
        assertEquals("Lunes 9 marzo", LocalDate(2026, 3, 9).formatEsWeekdayDayMonth())
        assertEquals("Martes 10 marzo", LocalDate(2026, 3, 10).formatEsWeekdayDayMonth())
        assertEquals("Miercoles 11 marzo", LocalDate(2026, 3, 11).formatEsWeekdayDayMonth())
        assertEquals("Jueves 12 marzo", LocalDate(2026, 3, 12).formatEsWeekdayDayMonth())
        assertEquals("Viernes 13 marzo", LocalDate(2026, 3, 13).formatEsWeekdayDayMonth())
        assertEquals("Sabado 14 marzo", LocalDate(2026, 3, 14).formatEsWeekdayDayMonth())
        assertEquals("Domingo 15 marzo", LocalDate(2026, 3, 15).formatEsWeekdayDayMonth())
    }

    @Test
    fun `format helpers map all spanish months`() {
        val expectedMonths = listOf(
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

        expectedMonths.forEachIndexed { index, expectedMonth ->
            val month = index + 1
            val date = LocalDate(2026, month, 1)
            val yearMonth = YearMonthValue(2026, month)
            assertEquals("1 de $expectedMonth, 2026", date.formatEsLongDate())
            assertEquals("${expectedMonth.replaceFirstChar { it.uppercase() }} 2026", yearMonth.formatEsMonthYear())
        }
    }
}
