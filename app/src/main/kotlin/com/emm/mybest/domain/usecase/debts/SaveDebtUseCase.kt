package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.domain.repository.DebtRepository
import com.emm.mybest.domain.validation.DebtDraft
import com.emm.mybest.domain.validation.DebtValidationError
import com.emm.mybest.domain.validation.DebtValidator

class SaveDebtUseCase(
    private val debtRepository: DebtRepository,
) {

    /**
     * Stores [draft] as the debt with [id] (0 for a new one) and returns no errors, or returns the validation errors
     * and stores nothing.
     */
    suspend operator fun invoke(id: Long, draft: DebtDraft): List<DebtValidationError> {
        val errors = DebtValidator.validate(draft)
        if (errors.isNotEmpty()) return errors
        draft.toDebtOrNull(id)?.let { debtRepository.saveDebt(it) }
        return emptyList()
    }
}
