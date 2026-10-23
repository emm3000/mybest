package com.emm.mybest.domain.validation

import com.emm.mybest.domain.models.DailyCheckType

sealed interface SetupValidationError {
    data class WeeklyTargetOutOfRange(val type: DailyCheckType) : SetupValidationError
    data object NegativeMonthlyOutingLimit : SetupValidationError
    data object NonPositiveTargetWeight : SetupValidationError
    data object NegativeTypicalOutingCost : SetupValidationError
}
