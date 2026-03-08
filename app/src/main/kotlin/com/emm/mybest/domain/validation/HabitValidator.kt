package com.emm.mybest.domain.validation

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object HabitValidator {

    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "El nombre no puede estar vacío")
        }
        if (name.length < 3) {
            return ValidationResult(false, "El nombre debe tener al menos 3 caracteres")
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
