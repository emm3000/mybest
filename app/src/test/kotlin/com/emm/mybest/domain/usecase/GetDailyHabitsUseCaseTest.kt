package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.models.HabitWithRecord
import com.emm.mybest.domain.repository.HabitRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDailyHabitsUseCaseTest {

    private val repository = mockk<HabitRepository>()
    private val useCase = GetDailyHabitsUseCase(repository)

    @Test
    fun `invoke delegates to repository and returns habits for date`() = runTest {
        val date = LocalDate(2026, 3, 8)
        val expected = listOf(
            HabitWithRecord(
                habit = Habit(
                    id = "h-1",
                    name = "Entrenar",
                    icon = "FitnessCenter",
                    color = 1,
                    category = "Salud",
                    type = HabitType.BOOLEAN,
                    scheduledDays = setOf(DayOfWeek.MONDAY),
                ),
                record = HabitRecord(
                    id = "r-1",
                    habitId = "h-1",
                    date = date,
                    value = 1f,
                    isCompleted = true,
                ),
            ),
        )
        every { repository.getHabitsWithRecordsForDate(date) } returns flowOf(expected)

        val actual = useCase(date).single()

        assertEquals(expected, actual)
        verify(exactly = 1) { repository.getHabitsWithRecordsForDate(date) }
    }
}
