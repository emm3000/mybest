package com.emm.mybest.domain.validation

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

private const val MIN_HABIT_NAME_LENGTH = 3

object HabitValidator {

    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "El nombre no puede estar vacío")
        }
        if (name.length < MIN_HABIT_NAME_LENGTH) {
            return ValidationResult(false, "El nombre debe tener al menos $MIN_HABIT_NAME_LENGTH caracteres")
        }
        return ValidationResult(true)
    }

    fun validateGoal(type: com.emm.mybest.domain.models.HabitType, value: Float?): ValidationResult {
        if (type != com.emm.mybest.domain.models.HabitType.BOOLEAN && (value == null || value <= 0)) {
            return ValidationResult(false, "El objetivo debe ser mayor a 0")
        }
        return ValidationResult(true)
    }
}
