package com.emm.mybest.domain.validation

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Setup
import org.junit.Assert.assertEquals
import org.junit.Test

class SetupValidatorTest {

    @Test
    fun `all-unset setup is valid`() {
        assertEquals(emptyList<SetupValidationError>(), SetupValidator.validate(Setup()))
    }

    @Test
    fun `weekly target above seven is out of range for its daily check`() {
        val setup = Setup(weeklyTargets = mapOf(DailyCheckType.GYM to 8))

        assertEquals(
            listOf(SetupValidationError.WeeklyTargetOutOfRange(DailyCheckType.GYM)),
            SetupValidator.validate(setup),
        )
    }

    @Test
    fun `negative weekly target is out of range for its daily check`() {
        val setup = Setup(weeklyTargets = mapOf(DailyCheckType.ENGLISH to -1))

        assertEquals(
            listOf(SetupValidationError.WeeklyTargetOutOfRange(DailyCheckType.ENGLISH)),
            SetupValidator.validate(setup),
        )
    }

    @Test
    fun `weekly targets of zero and seven are valid`() {
        val setup = Setup(weeklyTargets = mapOf(DailyCheckType.DIET to 0, DailyCheckType.ALCOHOL to 7))

        assertEquals(emptyList<SetupValidationError>(), SetupValidator.validate(setup))
    }

    @Test
    fun `negative monthly outing limit is rejected`() {
        assertEquals(
            listOf(SetupValidationError.NegativeMonthlyOutingLimit),
            SetupValidator.validate(Setup(monthlyOutingLimit = -1)),
        )
    }

    @Test
    fun `monthly outing limit of zero is valid`() {
        assertEquals(emptyList<SetupValidationError>(), SetupValidator.validate(Setup(monthlyOutingLimit = 0)))
    }

    @Test
    fun `zero target weight is rejected`() {
        assertEquals(
            listOf(SetupValidationError.NonPositiveTargetWeight),
            SetupValidator.validate(Setup(targetWeightKg = 0f)),
        )
    }

    @Test
    fun `NaN target weight is rejected`() {
        assertEquals(
            listOf(SetupValidationError.NonPositiveTargetWeight),
            SetupValidator.validate(Setup(targetWeightKg = Float.NaN)),
        )
    }

    @Test
    fun `infinite target weight is rejected`() {
        assertEquals(
            listOf(SetupValidationError.NonPositiveTargetWeight),
            SetupValidator.validate(Setup(targetWeightKg = Float.POSITIVE_INFINITY)),
        )
    }

    @Test
    fun `positive target weight is valid`() {
        assertEquals(emptyList<SetupValidationError>(), SetupValidator.validate(Setup(targetWeightKg = 72.5f)))
    }

    @Test
    fun `negative typical outing cost is rejected`() {
        assertEquals(
            listOf(SetupValidationError.NegativeTypicalOutingCost),
            SetupValidator.validate(Setup(typicalOutingCost = Money(-1))),
        )
    }

    @Test
    fun `typical outing cost of zero is valid`() {
        assertEquals(emptyList<SetupValidationError>(), SetupValidator.validate(Setup(typicalOutingCost = Money.ZERO)))
    }

    @Test
    fun `every violation is reported with its own error`() {
        val setup = Setup(
            targetWeightKg = -70f,
            monthlyOutingLimit = -2,
            typicalOutingCost = Money(-5_000),
            weeklyTargets = mapOf(DailyCheckType.GYM to 9),
        )

        assertEquals(
            setOf(
                SetupValidationError.WeeklyTargetOutOfRange(DailyCheckType.GYM),
                SetupValidationError.NegativeMonthlyOutingLimit,
                SetupValidationError.NonPositiveTargetWeight,
                SetupValidationError.NegativeTypicalOutingCost,
            ),
            SetupValidator.validate(setup).toSet(),
        )
    }
}
