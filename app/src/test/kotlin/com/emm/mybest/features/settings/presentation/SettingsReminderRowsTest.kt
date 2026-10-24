package com.emm.mybest.features.settings.presentation

import com.emm.mybest.R
import com.emm.mybest.domain.models.Setup
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsReminderRowsTest {

    private val unset = SettingsRowValue.Resource(R.string.settings_goal_unset)

    @Test
    fun `a fresh setup shows every reminder unset and the photo reminder off`() {
        val reminders = Setup().toReminders()

        assertEquals(List(4) { unset }, reminders.rows.map { it.value })
        assertEquals(List(4) { false }, reminders.rows.map { it.isSet })
        assertEquals(unset, reminders.debtRow.value)
        assertFalse(reminders.debtRow.isSet)
        assertFalse(reminders.isPhotoReminderEnabled)
    }

    @Test
    fun `rows keep the screen order with their labels`() {
        val reminders = Setup().toReminders()

        assertEquals(
            listOf(
                ReminderField.PRE_OUTING_TIME,
                ReminderField.CHECK_IN_TIME,
                ReminderField.WEIGH_IN_DAY,
                ReminderField.WEIGH_IN_TIME,
            ),
            reminders.rows.map { it.field },
        )
        assertEquals(ReminderField.DEBT_TIME, reminders.debtRow.field)
        assertEquals(R.string.settings_reminder_debt_label, reminders.debtRow.labelRes)
    }

    @Test
    fun `set reminders show their time or weekday and can be cleared`() {
        val setup = Setup(
            preOutingReminderTime = LocalTime(20, 30),
            weighInDay = DayOfWeek.MONDAY,
            debtReminderTime = LocalTime(9, 5),
            isPhotoReminderEnabled = true,
        )

        val reminders = setup.toReminders()

        assertEquals(
            listOf(
                SettingsRowValue.Text("20:30"),
                unset,
                SettingsRowValue.Resource(R.string.settings_weekday_monday),
                unset,
            ),
            reminders.rows.map { it.value },
        )
        assertEquals(listOf(true, false, true, false), reminders.rows.map { it.isSet })
        assertEquals(
            SettingsReminderRow(
                ReminderField.DEBT_TIME,
                R.string.settings_reminder_debt_label,
                SettingsRowValue.Text("09:05"),
                isSet = true,
            ),
            reminders.debtRow,
        )
        assertTrue(reminders.isPhotoReminderEnabled)
    }
}
