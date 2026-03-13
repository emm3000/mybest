package com.emm.mybest.data.reminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitReminderSchedulerImplTest {

    private val workManager = mockk<WorkManager>()
    private val scheduler = HabitReminderSchedulerImpl(workManager)

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
        val habit = Habit(
            id = "habit-1",
            name = "Ejercicio",
            icon = "FitnessCenter",
            color = 1,
            category = "Salud",
            type = HabitType.BOOLEAN,
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
}
