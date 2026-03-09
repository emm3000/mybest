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

class CreateHabitUseCaseTest {

    private val repository = mockk<HabitRepository>()
    private val useCase = CreateHabitUseCase(repository)

    @Test
    fun `invoke should call repository insertHabit`() = runBlocking {
        val habit = Habit(
            id = "1",
            name = "Test",
            icon = "Icon",
            color = 0,
            category = "Cat",
            type = HabitType.BOOLEAN,
            scheduledDays = setOf(DayOfWeek.MONDAY),
        )

        coEvery { repository.insertHabit(any()) } returns Unit

        useCase(habit)

        coVerify { repository.insertHabit(habit) }
    }
}
