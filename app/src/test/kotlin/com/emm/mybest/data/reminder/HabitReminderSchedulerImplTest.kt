package com.emm.mybest.data.reminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitReminderSchedulerImplTest {

    private val workManager = mockk<WorkManager>()
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val scheduler = HabitReminderSchedulerImpl(workManager, userPreferencesRepository)

    @Test
    fun `schedulePreventiveReminder enqueues unique periodic work`() = runBlocking {
        val requestSlot = slot<PeriodicWorkRequest>()
        every {
            workManager.enqueueUniquePeriodicWork(
                any(),
                any(),
                capture(requestSlot),
            )
        } returns mockk(relaxed = true)
        every { userPreferencesRepository.defaultReminderTime } returns flowOf(Pair(20, 0))

        val habit = Habit(
            id = "habit-1",
            name = "Ejercicio",
            icon = "FitnessCenter",
            color = 1,
            category = "Salud",
            type = HabitType.BOOLEAN,
            reminderEnabled = true,
            scheduledDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        )

        scheduler.schedulePreventiveReminder(habit)

        verify(exactly = 1) {
            workManager.enqueueUniquePeriodicWork(
                habitReminderUniqueWorkName("habit-1"),
                ExistingPeriodicWorkPolicy.UPDATE,
                any(),
            )
        }
        assertTrue(requestSlot.captured.tags.contains(habitReminderUniqueWorkName("habit-1")))
    }

    @Test
    fun `cancelReminder cancels unique work by habit id`() = runBlocking {
        every { workManager.cancelUniqueWork(any()) } returns mockk(relaxed = true)

        scheduler.cancelReminder("habit-2")

        verify(exactly = 1) {
            workManager.cancelUniqueWork(habitReminderUniqueWorkName("habit-2"))
        }
    }

    @Test
    fun `schedulePreventiveReminder uses per-habit time when set`() = runBlocking {
        val requestSlot = slot<PeriodicWorkRequest>()
        every {
            workManager.enqueueUniquePeriodicWork(any(), any(), capture(requestSlot))
        } returns mockk(relaxed = true)
        every { userPreferencesRepository.defaultReminderTime } returns flowOf(Pair(20, 0))

        val habit = Habit(
            id = "habit-1",
            name = "Ejercicio",
            icon = "FitnessCenter",
            color = 1,
            category = "Salud",
            type = HabitType.BOOLEAN,
            reminderEnabled = true,
            reminderHour = 9,
            reminderMinute = 0,
            scheduledDays = setOf(DayOfWeek.MONDAY),
        )

        scheduler.schedulePreventiveReminder(habit)

        verify(exactly = 1) {
            workManager.enqueueUniquePeriodicWork(
                habitReminderUniqueWorkName("habit-1"),
                ExistingPeriodicWorkPolicy.UPDATE,
                any(),
            )
        }
        val delayMillis = requestSlot.captured.workSpec.initialDelay
        assertTrue("Delay must be > 0", delayMillis > 0)
        assertTrue("Delay must be < 24h", delayMillis < 24 * 60 * 60 * 1000L)
    }

    @Test
    fun `schedulePreventiveReminder falls back to global default when habit time is null`() = runBlocking {
        val requestSlot = slot<PeriodicWorkRequest>()
        every {
            workManager.enqueueUniquePeriodicWork(any(), any(), capture(requestSlot))
        } returns mockk(relaxed = true)
        every { userPreferencesRepository.defaultReminderTime } returns flowOf(Pair(20, 0))

        val habit = Habit(
            id = "habit-3",
            name = "Leer",
            icon = "Book",
            color = 1,
            category = "Mente",
            type = HabitType.BOOLEAN,
            reminderEnabled = true,
            reminderHour = null,
            reminderMinute = null,
            scheduledDays = setOf(DayOfWeek.TUESDAY),
        )

        scheduler.schedulePreventiveReminder(habit)

        verify(exactly = 1) {
            workManager.enqueueUniquePeriodicWork(
                habitReminderUniqueWorkName("habit-3"),
                ExistingPeriodicWorkPolicy.UPDATE,
                any(),
            )
        }
        val delayMillis = requestSlot.captured.workSpec.initialDelay
        assertTrue("Delay must be > 0", delayMillis > 0)
        assertTrue("Delay must be < 24h", delayMillis < 24 * 60 * 60 * 1000L)
    }

    @Test
    fun `schedulePreventiveReminder cancels and skips when reminderEnabled is false`() = runBlocking {
        every { workManager.cancelUniqueWork(any()) } returns mockk(relaxed = true)

        val habit = Habit(
            id = "habit-4",
            name = "Meditar",
            icon = "SelfImprovement",
            color = 1,
            category = "Mente",
            type = HabitType.BOOLEAN,
            isEnabled = true,
            reminderEnabled = false,
            scheduledDays = setOf(DayOfWeek.FRIDAY),
        )

        scheduler.schedulePreventiveReminder(habit)

        verify(exactly = 0) {
            workManager.enqueueUniquePeriodicWork(any(), any(), any())
        }
        verify(exactly = 1) {
            workManager.cancelUniqueWork(habitReminderUniqueWorkName("habit-4"))
        }
    }
}
