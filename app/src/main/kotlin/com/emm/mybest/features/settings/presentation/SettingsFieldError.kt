package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.validation.SetupValidationError

enum class SettingsFieldError {
    INVALID_NUMBER,
    OUT_OF_RANGE,
    NOT_POSITIVE,
    NEGATIVE,
}

internal fun SetupValidationError.toFieldError(): SettingsFieldError = when (this) {
    is SetupValidationError.WeeklyTargetOutOfRange -> SettingsFieldError.OUT_OF_RANGE
    SetupValidationError.NonPositiveTargetWeight -> SettingsFieldError.NOT_POSITIVE
    SetupValidationError.NegativeMonthlyOutingLimit,
    SetupValidationError.NegativeTypicalOutingCost,
    -> SettingsFieldError.NEGATIVE
}
