package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.validation.SetupValidationError

enum class SettingsFieldError {
    INVALID_NUMBER,
    OUT_OF_RANGE,
    WEIGHT_OUT_OF_RANGE,
    NEGATIVE,
}

internal fun SetupValidationError.toFieldError(): SettingsFieldError = when (this) {
    is SetupValidationError.WeeklyTargetOutOfRange -> SettingsFieldError.OUT_OF_RANGE
    SetupValidationError.TargetWeightOutOfRange -> SettingsFieldError.WEIGHT_OUT_OF_RANGE
    SetupValidationError.NegativeMonthlyOutingLimit,
    SetupValidationError.NegativeTypicalOutingCost,
    -> SettingsFieldError.NEGATIVE
}

internal fun SetupValidationError.field(): SettingsField = when (this) {
    is SetupValidationError.WeeklyTargetOutOfRange -> SettingsField.entries.first { it.checkType == type }
    SetupValidationError.NegativeMonthlyOutingLimit -> SettingsField.MONTHLY_OUTING_LIMIT
    SetupValidationError.TargetWeightOutOfRange -> SettingsField.TARGET_WEIGHT
    SetupValidationError.NegativeTypicalOutingCost -> SettingsField.TYPICAL_OUTING_COST
}
