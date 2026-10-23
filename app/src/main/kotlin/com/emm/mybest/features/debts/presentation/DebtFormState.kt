package com.emm.mybest.features.debts.presentation

import com.emm.mybest.domain.validation.DebtValidationError

data class DebtFormState(
    val id: Long = 0,
    val name: String = "",
    val balance: String = "",
    val installment: String = "",
    val paymentDay: String = "",
    val errors: Set<DebtValidationError> = emptySet(),
)
