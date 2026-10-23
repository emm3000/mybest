package com.emm.mybest.domain.usecase.goal

import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.validation.PurchaseGoalDraft
import com.emm.mybest.domain.validation.PurchaseGoalValidationError
import com.emm.mybest.domain.validation.PurchaseGoalValidator

class SavePurchaseGoalUseCase(
    private val purchaseGoalRepository: PurchaseGoalRepository,
) {

    /**
     * Stores [draft] as the purchase goal and returns no errors, or returns the validation errors and stores nothing.
     */
    suspend operator fun invoke(draft: PurchaseGoalDraft): List<PurchaseGoalValidationError> {
        val errors = PurchaseGoalValidator.validate(draft)
        if (errors.isNotEmpty()) return errors
        draft.toGoalOrNull()?.let { purchaseGoalRepository.saveGoal(it) }
        return emptyList()
    }
}
