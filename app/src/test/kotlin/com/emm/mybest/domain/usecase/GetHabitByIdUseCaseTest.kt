package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.repository.HabitRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetHabitByIdUseCaseTest {

    private val repository = mockk<HabitRepository>()
    private val useCase = GetHabitByIdUseCase(repository)

    @Test
    fun `invoke should return habit when repository emits one`() = runBlocking {
        val habit = Habit(
            id = "habit-1",
            name = "Leer",
            icon = "FitnessCenter",
            color = 0,
            category = "Mente",
            type = HabitType.BOOLEAN,
            scheduledDays = setOf(DayOfWeek.MONDAY),
        )
        every { repository.getHabitById("habit-1") } returns flowOf(habit)

        val result = useCase("habit-1")

        assertEquals(habit, result)
    }

    @Test
    fun `invoke should return null when repository emits null`() = runBlocking {
        every { repository.getHabitById("missing") } returns flowOf(null)

        val result = useCase("missing")

        assertNull(result)
    }
}
