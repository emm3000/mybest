package com.emm.mybest.domain.validation

/**
 * Checks the values a user entered for the purchase goal. An empty result means the draft can be saved.
 */
object PurchaseGoalValidator {

    fun validate(draft: PurchaseGoalDraft): List<PurchaseGoalValidationError> = listOfNotNull(
        PurchaseGoalValidationError.BlankName.takeIf { draft.name.isBlank() },
        PurchaseGoalValidationError.NonPositivePrice.takeIf { (draft.price?.cents ?: 0) <= 0 },
    )
}
