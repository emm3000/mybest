package com.emm.mybest.domain.reminder

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

private val WEEKEND_DAYS = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
private val EVENING = LocalTime(19, 0)
private val MID_MORNING = LocalTime(10, 0)
private val EVERY_DAY = DayOfWeek.entries.toSet()

class ReminderOccurrenceTest {

    @Test
    fun `thursday evening reminds on friday`() {
        val next = nextOccurrence(LocalDateTime(2026, 10, 22, 20, 0), WEEKEND_DAYS, EVENING)

        assertEquals(LocalDateTime(2026, 10, 23, 19, 0), next)
    }

    @Test
    fun `friday before the time reminds the same day`() {
        val next = nextOccurrence(LocalDateTime(2026, 10, 23, 18, 0), WEEKEND_DAYS, EVENING)

        assertEquals(LocalDateTime(2026, 10, 23, 19, 0), next)
    }

    @Test
    fun `friday after the time reminds on saturday`() {
        val next = nextOccurrence(LocalDateTime(2026, 10, 23, 20, 0), WEEKEND_DAYS, EVENING)

        assertEquals(LocalDateTime(2026, 10, 24, 19, 0), next)
    }

    @Test
    fun `saturday after the time reminds on the next friday`() {
        val next = nextOccurrence(LocalDateTime(2026, 10, 24, 20, 0), WEEKEND_DAYS, EVENING)

        assertEquals(LocalDateTime(2026, 10, 30, 19, 0), next)
    }

    @Test
    fun `daily reminder before the time reminds the same day`() {
        val next = nextOccurrence(LocalDateTime(2026, 10, 23, 9, 0), EVERY_DAY, MID_MORNING)

        assertEquals(LocalDateTime(2026, 10, 23, 10, 0), next)
    }

    @Test
    fun `daily reminder after the time reminds the next day`() {
        val next = nextOccurrence(LocalDateTime(2026, 10, 23, 11, 0), EVERY_DAY, MID_MORNING)

        assertEquals(LocalDateTime(2026, 10, 24, 10, 0), next)
    }

    @Test
    fun `exactly at the time reminds at the next occurrence`() {
        val next = nextOccurrence(LocalDateTime(2026, 10, 23, 10, 0), EVERY_DAY, MID_MORNING)

        assertEquals(LocalDateTime(2026, 10, 24, 10, 0), next)
    }

    @Test
    fun `a single day exactly at the time reminds a week later`() {
        val next = nextOccurrence(LocalDateTime(2026, 10, 23, 19, 0), setOf(DayOfWeek.FRIDAY), EVENING)

        assertEquals(LocalDateTime(2026, 10, 30, 19, 0), next)
    }
}
