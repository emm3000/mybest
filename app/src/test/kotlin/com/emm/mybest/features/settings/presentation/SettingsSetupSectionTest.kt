package com.emm.mybest.features.settings.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSetupSectionTest {

    @Test
    fun `checks section holds the four weekly targets`() {
        assertEquals(
            listOf(
                SettingsField.WEEKLY_GYM,
                SettingsField.WEEKLY_DIET,
                SettingsField.WEEKLY_ENGLISH,
                SettingsField.WEEKLY_ALCOHOL,
            ),
            SettingsSetupSection.CHECKS.fields,
        )
    }

    @Test
    fun `outings section holds the monthly limit and the typical cost`() {
        assertEquals(
            listOf(SettingsField.MONTHLY_OUTING_LIMIT, SettingsField.TYPICAL_OUTING_COST),
            SettingsSetupSection.OUTINGS.fields,
        )
    }

    @Test
    fun `weight section holds the target weight and the reminders`() {
        assertEquals(listOf(SettingsField.TARGET_WEIGHT), SettingsSetupSection.WEIGHT.fields)
        assertTrue(SettingsSetupSection.WEIGHT.hasReminders)
    }

    @Test
    fun `money section holds no goal fields but the debts and purchase goal rows`() {
        assertEquals(emptyList<SettingsField>(), SettingsSetupSection.MONEY.fields)
        assertTrue(SettingsSetupSection.MONEY.hasMoneyRows)
        assertFalse(SettingsSetupSection.MONEY.hasReminders)
    }

    @Test
    fun `every settings field appears in exactly one section`() {
        val placed = SettingsSetupSection.entries.flatMap { it.fields }

        assertEquals(SettingsField.entries.toSet(), placed.toSet())
        assertEquals(SettingsField.entries.size, placed.size)
    }

    @Test
    fun `only the money section shows the money rows and only the weight section the reminders`() {
        assertEquals(listOf(SettingsSetupSection.MONEY), SettingsSetupSection.entries.filter { it.hasMoneyRows })
        assertEquals(listOf(SettingsSetupSection.WEIGHT), SettingsSetupSection.entries.filter { it.hasReminders })
    }
}
