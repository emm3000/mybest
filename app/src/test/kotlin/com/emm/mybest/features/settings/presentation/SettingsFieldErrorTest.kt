package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.validation.SetupValidationError
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsFieldErrorTest {

    @Test
    fun `every check type maps to the weekly field of that type`() {
        val expected = mapOf(
            DailyCheckType.GYM to SettingsField.WEEKLY_GYM,
            DailyCheckType.DIET to SettingsField.WEEKLY_DIET,
            DailyCheckType.ENGLISH to SettingsField.WEEKLY_ENGLISH,
            DailyCheckType.ALCOHOL to SettingsField.WEEKLY_ALCOHOL,
        )

        assertEquals(expected.keys, DailyCheckType.entries.toSet())
        DailyCheckType.entries.forEach { type ->
            assertEquals(expected.getValue(type), SetupValidationError.WeeklyTargetOutOfRange(type).field())
        }
    }

    @Test
    fun `non weekly errors keep mapping to their fields`() {
        assertEquals(SettingsField.MONTHLY_OUTING_LIMIT, SetupValidationError.NegativeMonthlyOutingLimit.field())
        assertEquals(SettingsField.TARGET_WEIGHT, SetupValidationError.TargetWeightOutOfRange.field())
        assertEquals(SettingsField.TYPICAL_OUTING_COST, SetupValidationError.NegativeTypicalOutingCost.field())
    }
}
