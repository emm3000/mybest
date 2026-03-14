package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.HabitEntity
import com.emm.mybest.data.entities.HabitType.BOOLEAN
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import kotlinx.datetime.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitMappersTest {

    @Test
    fun `toDomain maps all reminder fields correctly`() {
        val entity = HabitEntity(
            id = "h1",
            name = "Ejercicio",
            icon = "FitnessCenter",
            color = 1,
            category = "Salud",
            type = BOOLEAN,
            isEnabled = true,
            reminderEnabled = true,
            reminderHour = 9,
            reminderMinute = 0,
            scheduledDays = "1,3",
        )

        val domain = entity.toDomain()

        assertTrue(domain.reminderEnabled)
        assertEquals(9, domain.reminderHour)
        assertEquals(0, domain.reminderMinute)
    }

    @Test
    fun `toEntity maps all reminder fields correctly`() {
        val habit = Habit(
            id = "h2",
            name = "Leer",
            icon = "Book",
            color = 2,
            category = "Mente",
            type = HabitType.BOOLEAN,
            isEnabled = true,
            reminderEnabled = false,
            reminderHour = 7,
            reminderMinute = 45,
            scheduledDays = setOf(DayOfWeek.MONDAY),
        )

        val entity = habit.toEntity()

        assertEquals(false, entity.reminderEnabled)
        assertEquals(7, entity.reminderHour)
        assertEquals(45, entity.reminderMinute)
    }

    @Test
    fun `toDomain handles null reminder time`() {
        val entity = HabitEntity(
            id = "h3",
            name = "Meditar",
            icon = "SelfImprovement",
            color = 3,
            category = "Mente",
            type = BOOLEAN,
            isEnabled = true,
            reminderEnabled = true,
            reminderHour = null,
            reminderMinute = null,
            scheduledDays = "2",
        )

        val domain = entity.toDomain()

        assertTrue(domain.reminderEnabled)
        assertNull(domain.reminderHour)
        assertNull(domain.reminderMinute)
    }
}
