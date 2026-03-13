package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.reminder.HabitReminderScheduler
import com.emm.mybest.domain.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import org.junit.Test

class CreateHabitUseCaseTest {

    private val repository = mockk<HabitRepository>()
    private val reminderScheduler = mockk<HabitReminderScheduler>()
    private val useCase = CreateHabitUseCase(repository, reminderScheduler)

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
        coEvery { reminderScheduler.schedulePreventiveReminder(any()) } returns Unit

        useCase(habit)

        coVerify { repository.insertHabit(habit) }
        coVerify { reminderScheduler.schedulePreventiveReminder(habit) }
    }

    @Test
    fun `invoke should cancel reminder when habit is disabled`() = runBlocking {
        val habit = Habit(
            id = "2",
            name = "Test disabled",
            icon = "Icon",
            color = 0,
            category = "Cat",
            type = HabitType.BOOLEAN,
            isEnabled = false,
            scheduledDays = setOf(DayOfWeek.MONDAY),
        )

        coEvery { repository.insertHabit(any()) } returns Unit
        coEvery { reminderScheduler.cancelReminder(any()) } returns Unit

        useCase(habit)

        coVerify { repository.insertHabit(habit) }
        coVerify { reminderScheduler.cancelReminder("2") }
    }
}
