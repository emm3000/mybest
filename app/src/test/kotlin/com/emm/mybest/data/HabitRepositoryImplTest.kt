package com.emm.mybest.data

import com.emm.mybest.data.entities.HabitDao
import com.emm.mybest.data.entities.HabitEntity
import com.emm.mybest.data.entities.HabitRecordDao
import com.emm.mybest.data.entities.HabitRecordEntity
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HabitRepositoryImplTest {

    private val habitDao = mockk<HabitDao>()
    private val habitRecordDao = mockk<HabitRecordDao>()
    private val repository = HabitRepositoryImpl(habitDao, habitRecordDao)

    @Test
    fun `getAllHabits maps entities from dao`() = runTest {
        val entity = HabitEntity(
            id = "h1",
            name = "Caminar",
            icon = "walk",
            color = 1,
            category = "Salud",
            type = com.emm.mybest.data.entities.HabitType.TIME,
            goalValue = 30f,
            unit = "min",
            isEnabled = true,
            scheduledDays = "1,3,5",
            createdAt = 100L,
        )
        every { habitDao.getAllHabits() } returns flowOf(listOf(entity))

        val result = repository.getAllHabits().first()

        assertEquals(1, result.size)
        assertEquals("h1", result.first().id)
        assertEquals(HabitType.TIME, result.first().type)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), result.first().scheduledDays)
    }

    @Test
    fun `getHabitById maps nullable entity to nullable domain`() = runTest {
        every { habitDao.getHabitById("h2") } returns flowOf(null)

        val result = repository.getHabitById("h2").first()

        assertNull(result)
    }

    @Test
    fun `getHabitsWithRecordsForDate combines habits and matching records`() = runTest {
        val date = LocalDate(2026, 3, 8)
        val habitEntity = HabitEntity(
            id = "h1",
            name = "Agua",
            icon = "water",
            color = 2,
            category = "Salud",
            type = com.emm.mybest.data.entities.HabitType.METRIC,
            goalValue = 2f,
            unit = "L",
            isEnabled = true,
            scheduledDays = "1,2,3,4,5,6,7",
        )
        val recordEntity = HabitRecordEntity(
            id = "r1",
            habitId = "h1",
            date = date,
            value = 2f,
            isCompleted = true,
            notes = "done",
        )
        every { habitDao.getAllEnabledHabits() } returns flowOf(listOf(habitEntity))
        every { habitRecordDao.getRecordsByDate(date) } returns flowOf(listOf(recordEntity))

        val result = repository.getHabitsWithRecordsForDate(date).first()

        assertEquals(1, result.size)
        assertEquals("h1", result.first().habit.id)
        assertEquals("r1", result.first().record?.id)
        assertEquals(true, result.first().record?.isCompleted)
    }

    @Test
    fun `insert update delete and insertRecord delegate with mapped entities`() = runTest {
        val habit = Habit(
            id = "h3",
            name = "Leer",
            icon = "book",
            color = 3,
            category = "Mind",
            type = HabitType.TIME,
            goalValue = 20f,
            unit = "min",
            isEnabled = true,
            scheduledDays = setOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY),
            createdAt = 200L,
        )
        val record = HabitRecord(
            id = "r3",
            habitId = "h3",
            date = LocalDate(2026, 3, 8),
            value = 20f,
            isCompleted = true,
            notes = "night",
        )
        val insertHabitSlot = slot<HabitEntity>()
        val updateHabitSlot = slot<HabitEntity>()
        val deleteHabitSlot = slot<HabitEntity>()
        val recordSlot = slot<HabitRecordEntity>()
        coEvery { habitDao.insert(capture(insertHabitSlot)) } returns Unit
        coEvery { habitDao.update(capture(updateHabitSlot)) } returns Unit
        coEvery { habitDao.delete(capture(deleteHabitSlot)) } returns Unit
        coEvery { habitRecordDao.insert(capture(recordSlot)) } returns Unit

        repository.insertHabit(habit)
        repository.updateHabit(habit)
        repository.deleteHabit(habit)
        repository.insertRecord(record)

        coVerify(exactly = 1) { habitDao.insert(any()) }
        coVerify(exactly = 1) { habitDao.update(any()) }
        coVerify(exactly = 1) { habitDao.delete(any()) }
        coVerify(exactly = 1) { habitRecordDao.insert(any()) }
        assertEquals("1,7", insertHabitSlot.captured.scheduledDays)
        assertEquals("1,7", updateHabitSlot.captured.scheduledDays)
        assertEquals("1,7", deleteHabitSlot.captured.scheduledDays)
        assertEquals("r3", recordSlot.captured.id)
        assertEquals(20f, recordSlot.captured.value)
    }
}
