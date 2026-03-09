package com.emm.mybest.domain.validation

import com.emm.mybest.domain.models.HabitType

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
)

private const val MIN_HABIT_NAME_LENGTH = 3

object HabitValidator {

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "El nombre no puede estar vacío")
            name.length < MIN_HABIT_NAME_LENGTH -> {
                ValidationResult(false, "El nombre debe tener al menos $MIN_HABIT_NAME_LENGTH caracteres")
            }
            else -> ValidationResult(true)
        }
    }

    fun validateGoal(type: HabitType, value: Float?): ValidationResult {
        val requiresGoalValue = type != HabitType.BOOLEAN
        val isInvalidGoalValue = value == null || value <= 0

        return if (requiresGoalValue && isInvalidGoalValue) {
            ValidationResult(false, "El objetivo debe ser mayor a 0")
        } else {
            ValidationResult(true)
        }
    }
}
