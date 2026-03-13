package com.emm.mybest.data.reminder

import org.junit.Assert.assertEquals
import org.junit.Test

class HabitReminderWorkerTest {

    @Test
    fun `shouldDispatchReminder returns true only when conditions are met`() {
        val shouldNotify = shouldDispatchReminder(
            habitId = "habit-1",
            isReminderDay = true,
            isCompletedToday = false,
            canPostNotifications = true,
        )
        val missingHabit = shouldDispatchReminder(
            habitId = null,
            isReminderDay = true,
            isCompletedToday = false,
            canPostNotifications = true,
        )
        val completedToday = shouldDispatchReminder(
            habitId = "habit-1",
            isReminderDay = true,
            isCompletedToday = true,
            canPostNotifications = true,
        )
        val notReminderDay = shouldDispatchReminder(
            habitId = "habit-1",
            isReminderDay = false,
            isCompletedToday = false,
            canPostNotifications = true,
        )

        assertEquals(true, shouldNotify)
        assertEquals(false, missingHabit)
        assertEquals(false, completedToday)
        assertEquals(false, notReminderDay)
    }
}
