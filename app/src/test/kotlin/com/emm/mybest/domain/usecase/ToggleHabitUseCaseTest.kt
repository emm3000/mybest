package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.models.HabitWithRecord
import com.emm.mybest.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleHabitUseCaseTest {

    private val repository = mockk<HabitRepository>()
    private val useCase = ToggleHabitUseCase(repository)

    @Test
    fun `invoke creates completed record when there is no previous record`() = runTest {
        val date = LocalDate(2026, 3, 8)
        val habit = Habit(
            id = "h-1",
            name = "Entrenar",
            icon = "FitnessCenter",
            color = 1,
            category = "Salud",
            type = HabitType.BOOLEAN,
            scheduledDays = setOf(DayOfWeek.MONDAY),
        )
        val captured = slot<HabitRecord>()
        coEvery { repository.insertRecord(capture(captured)) } returns Unit

        useCase(HabitWithRecord(habit = habit, record = null), date)

        coVerify(exactly = 1) { repository.insertRecord(any()) }
        assertEquals("h-1", captured.captured.habitId)
        assertEquals(date, captured.captured.date)
        assertEquals(1f, captured.captured.value)
        assertTrue(captured.captured.isCompleted)
    }

    @Test
    fun `invoke toggles existing record from completed to not completed`() = runTest {
        val date = LocalDate(2026, 3, 8)
        val habit = Habit(
            id = "h-1",
            name = "Entrenar",
            icon = "FitnessCenter",
            color = 1,
            category = "Salud",
            type = HabitType.BOOLEAN,
            scheduledDays = setOf(DayOfWeek.MONDAY),
        )
        val existing = HabitRecord(
            id = "r-1",
            habitId = "h-1",
            date = date,
            value = 1f,
            isCompleted = true,
        )
        val captured = slot<HabitRecord>()
        coEvery { repository.insertRecord(capture(captured)) } returns Unit

        useCase(HabitWithRecord(habit = habit, record = existing), date)

        coVerify(exactly = 1) { repository.insertRecord(any()) }
        assertEquals("r-1", captured.captured.id)
        assertEquals(false, captured.captured.isCompleted)
        assertEquals(0f, captured.captured.value)
    }
}
