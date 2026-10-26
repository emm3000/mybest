package com.emm.mybest.features.goal.presentation

import com.emm.mybest.domain.validation.PurchaseGoalValidationError

data class GoalFormState(
    val name: String = "",
    val price: String = "",
    val errors: Set<PurchaseGoalValidationError> = emptySet(),
    val isEditing: Boolean = false,
)
