package com.emm.mybest.domain.validation

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt

/**
 * What a user entered for a debt; a null amount or payment day means the input was empty or not a number.
 */
data class DebtDraft(
    val name: String,
    val balance: Money?,
    val installment: Money?,
    val paymentDay: Int?,
) {
    /**
     * The debt with [id] this draft describes, or null when a value is missing or the payment day is out of range.
     * Callers that need to report why run [DebtValidator] first.
     */
    fun toDebtOrNull(id: Long): Debt? {
        if (balance == null || installment == null) return null
        val day = paymentDay?.takeIf { it in PAYMENT_DAY_RANGE } ?: return null
        return Debt(id, name.trim(), balance, installment, day)
    }
}
