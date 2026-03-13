package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import org.junit.Test

class UpdateHabitUseCaseTest {

    private val repository = mockk<HabitRepository>()
    private val useCase = UpdateHabitUseCase(repository)

    @Test
    fun `invoke should call repository updateHabit`() = runBlocking {
        val habit = Habit(
            id = "habit-1",
            name = "Leer",
            icon = "FitnessCenter",
            color = 0,
            category = "Mente",
            type = HabitType.TIME,
            goalValue = 30f,
            unit = "min",
            scheduledDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        )
        coEvery { repository.updateHabit(any()) } returns Unit

        useCase(habit)

        coVerify(exactly = 1) { repository.updateHabit(habit) }
    }
}
