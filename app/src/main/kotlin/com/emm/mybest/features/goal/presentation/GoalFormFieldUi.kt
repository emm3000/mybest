package com.emm.mybest.features.goal.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.KeyboardType
import com.emm.mybest.R
import com.emm.mybest.domain.validation.ContributionValidationError
import com.emm.mybest.domain.validation.PurchaseGoalValidationError

@get:StringRes
internal val GoalFormField.labelRes: Int
    get() = when (this) {
        GoalFormField.NAME -> R.string.purchase_goal_field_name
        GoalFormField.PRICE -> R.string.purchase_goal_field_price
    }

internal val GoalFormField.keyboardType: KeyboardType
    get() = when (this) {
        GoalFormField.NAME -> KeyboardType.Text
        GoalFormField.PRICE -> KeyboardType.Decimal
    }

internal fun GoalFormState.valueOf(field: GoalFormField): String = when (field) {
    GoalFormField.NAME -> name
    GoalFormField.PRICE -> price
}

internal fun GoalFormState.errorFor(field: GoalFormField): PurchaseGoalValidationError? =
    errors.firstOrNull { it.field() == field }

@StringRes
internal fun PurchaseGoalValidationError.messageRes(): Int = when (this) {
    PurchaseGoalValidationError.BlankName -> R.string.purchase_goal_error_blank_name
    PurchaseGoalValidationError.NonPositivePrice -> R.string.purchase_goal_error_price
}

@StringRes
internal fun ContributionValidationError.messageRes(): Int = when (this) {
    ContributionValidationError.NonPositiveAmount -> R.string.purchase_goal_error_contribution
}

private fun PurchaseGoalValidationError.field(): GoalFormField = when (this) {
    PurchaseGoalValidationError.BlankName -> GoalFormField.NAME
    PurchaseGoalValidationError.NonPositivePrice -> GoalFormField.PRICE
}
