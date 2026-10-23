package com.emm.mybest.domain.validation

internal val PAYMENT_DAY_RANGE = 1..31

/**
 * Checks the values a user entered for a debt. An empty result means the draft can be saved.
 */
object DebtValidator {

    fun validate(draft: DebtDraft): List<DebtValidationError> = listOfNotNull(
        DebtValidationError.BlankName.takeIf { draft.name.isBlank() },
        balanceError(draft),
        DebtValidationError.NonPositiveInstallment.takeIf { (draft.installment?.cents ?: 0) <= 0 },
        DebtValidationError.PaymentDayOutOfRange.takeIf { draft.paymentDay !in PAYMENT_DAY_RANGE },
    )

    private fun balanceError(draft: DebtDraft): DebtValidationError? = when {
        draft.balance == null -> DebtValidationError.MissingBalance
        draft.balance.isNegative -> DebtValidationError.NegativeBalance
        else -> null
    }
}
