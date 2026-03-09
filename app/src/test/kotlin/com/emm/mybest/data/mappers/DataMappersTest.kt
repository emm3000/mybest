package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.DailyHabitEntity
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.HabitEntity
import com.emm.mybest.data.entities.HabitRecordEntity
import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataMappersTest {

    @Test
    fun `weight and daily habit entities map to domain`() {
        val weightEntity = DailyWeightEntity(
            id = "w1",
            date = LocalDate(2026, 3, 8),
            weight = 70.5f,
            photoPath = "/tmp/w.jpg",
            note = "ok",
        )
        val habitEntity = DailyHabitEntity(
            date = LocalDate(2026, 3, 8),
            ateHealthy = true,
            didExercise = false,
            notes = "rest",
        )

        val weight = weightEntity.toDomain()
        val habit = habitEntity.toDomain()

        assertEquals("w1", weight.id)
        assertEquals(70.5f, weight.weight)
        assertEquals("/tmp/w.jpg", weight.photoPath)
        assertEquals(true, habit.ateHealthy)
        assertEquals(false, habit.didExercise)
        assertEquals("rest", habit.notes)
    }

    @Test
    fun `habit entity to domain parses scheduled days from numbers`() {
        val entity = HabitEntity(
            id = "h1",
            name = "Leer",
            icon = "book",
            color = 1,
            category = "Mind",
            type = com.emm.mybest.data.entities.HabitType.TIME,
            goalValue = 30f,
            unit = "min",
            isEnabled = true,
            scheduledDays = "1,3,7",
            createdAt = 123L,
        )

        val domain = entity.toDomain()

        assertEquals(HabitType.TIME, domain.type)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY), domain.scheduledDays)
    }

    @Test
    fun `habit entity to domain parses scheduled days from enum names`() {
        val entity = HabitEntity(
            id = "h2",
            name = "Cardio",
            icon = "run",
            color = 1,
            category = "Fitness",
            type = com.emm.mybest.data.entities.HabitType.BOOLEAN,
            scheduledDays = "MONDAY,FRIDAY",
        )

        val domain = entity.toDomain()

        assertEquals(HabitType.BOOLEAN, domain.type)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), domain.scheduledDays)
    }

    @Test
    fun `habit and record domain map to entities`() {
        val habit = Habit(
            id = "h3",
            name = "Agua",
            icon = "water",
            color = 2,
            category = "Salud",
            type = HabitType.METRIC,
            goalValue = 2f,
            unit = "L",
            isEnabled = true,
            scheduledDays = linkedSetOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
            createdAt = 777L,
        )
        val record = HabitRecord(
            id = "r1",
            habitId = "h3",
            date = LocalDate(2026, 3, 8),
            value = 2f,
            isCompleted = true,
            notes = "done",
        )

        val habitEntity = habit.toEntity()
        val recordEntity = record.toEntity()

        assertEquals(com.emm.mybest.data.entities.HabitType.METRIC, habitEntity.type)
        assertEquals("1,4", habitEntity.scheduledDays)
        assertEquals("r1", recordEntity.id)
        assertEquals("h3", recordEntity.habitId)
        assertEquals(true, recordEntity.isCompleted)
    }

    @Test
    fun `record and photo entities map to domain`() {
        val recordEntity = HabitRecordEntity(
            id = "r2",
            habitId = "h4",
            date = LocalDate(2026, 3, 7),
            value = 1f,
            isCompleted = true,
            notes = "ok",
        )
        val photoEntity = ProgressPhotoEntity(
            id = "p1",
            date = LocalDate(2026, 3, 8),
            type = com.emm.mybest.data.entities.PhotoType.FACE,
            photoPath = "/tmp/p.jpg",
            createdAt = 456L,
        )

        val record = recordEntity.toDomain()
        val photo = photoEntity.toDomain()

        assertEquals("r2", record.id)
        assertEquals(true, record.isCompleted)
        assertEquals("p1", photo.id)
        assertEquals(PhotoType.FACE, photo.type)
        assertEquals(456L, photo.createdAt)
    }

    @Test
    fun `new progress photo maps to entity`() {
        val input = NewProgressPhoto(
            photoPath = "/tmp/new.jpg",
            type = PhotoType.BODY,
            date = LocalDate(2026, 3, 8),
        )

        val entity = input.toEntity()

        assertEquals(com.emm.mybest.data.entities.PhotoType.BODY, entity.type)
        assertEquals("/tmp/new.jpg", entity.photoPath)
        assertEquals(LocalDate(2026, 3, 8), entity.date)
    }

    @Test
    fun `photo type mapping is symmetrical for all values`() {
        PhotoType.entries.forEach { type ->
            val mapped = type.toData().toDomain()
            assertEquals(type, mapped)
        }

        val hasAllTypes = PhotoType.entries.all {
            com.emm.mybest.data.entities.PhotoType.entries.map { dataType ->
                dataType.toDomain()
            }.contains(it)
        }
        assertTrue(hasAllTypes)
    }
}
