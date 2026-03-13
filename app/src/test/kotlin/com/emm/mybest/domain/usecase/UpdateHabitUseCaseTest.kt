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

class UpdateHabitUseCaseTest {

    private val repository = mockk<HabitRepository>()
    private val reminderScheduler = mockk<HabitReminderScheduler>()
    private val useCase = UpdateHabitUseCase(repository, reminderScheduler)

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
        coEvery { reminderScheduler.schedulePreventiveReminder(any()) } returns Unit

        useCase(habit)

        coVerify(exactly = 1) { repository.updateHabit(habit) }
        coVerify(exactly = 1) { reminderScheduler.schedulePreventiveReminder(habit) }
    }

    @Test
    fun `invoke should cancel reminder when habit is disabled`() = runBlocking {
        val habit = Habit(
            id = "habit-2",
            name = "Dormir",
            icon = "Bedtime",
            color = 0,
            category = "Salud",
            type = HabitType.BOOLEAN,
            isEnabled = false,
            scheduledDays = setOf(DayOfWeek.MONDAY),
        )
        coEvery { repository.updateHabit(any()) } returns Unit
        coEvery { reminderScheduler.cancelReminder(any()) } returns Unit

        useCase(habit)

        coVerify(exactly = 1) { repository.updateHabit(habit) }
        coVerify(exactly = 1) { reminderScheduler.cancelReminder("habit-2") }
    }
}
