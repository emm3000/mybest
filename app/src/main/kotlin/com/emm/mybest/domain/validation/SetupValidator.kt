package com.emm.mybest.domain.validation

import com.emm.mybest.domain.models.Setup

/**
 * Checks the values a user entered in their setup. Unset values are always valid; an empty result means the setup
 * can be saved.
 */
object SetupValidator {
    private val WEEKLY_TARGET_RANGE = 0..7

    fun validate(setup: Setup): List<SetupValidationError> = weeklyTargetErrors(setup) + listOfNotNull(
        SetupValidationError.NegativeMonthlyOutingLimit.takeIf { (setup.monthlyOutingLimit ?: 0) < 0 },
        SetupValidationError.NonPositiveTargetWeight.takeIf { setup.targetWeightKg?.let { it.isNaN() || it <= 0f } == true },
        SetupValidationError.NegativeTypicalOutingCost.takeIf { setup.typicalOutingCost?.isNegative == true },
    )

    private fun weeklyTargetErrors(setup: Setup): List<SetupValidationError> =
        setup.weeklyTargets
            .filterValues { it !in WEEKLY_TARGET_RANGE }
            .keys
            .map { SetupValidationError.WeeklyTargetOutOfRange(it) }
}
