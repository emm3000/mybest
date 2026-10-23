package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

private const val FIRST_PAYMENT_DAY = 1
private const val LAST_PAYMENT_DAY = 31

/**
 * A loan or credit with its remaining [balance], the monthly [installment] due on it and the [paymentDay] of the month
 * it falls due (1..31).
 */
data class Debt(
    val id: Long,
    val name: String,
    val balance: Money,
    val installment: Money,
    val paymentDay: Int,
) {
    init {
        require(paymentDay in FIRST_PAYMENT_DAY..LAST_PAYMENT_DAY) { "Payment day must be in 1..31" }
    }

    /**
     * This debt once one installment is paid: the balance drops by the installment and never below zero.
     */
    fun afterInstallmentPaid(): Debt = copy(balance = maxOf(balance - installment, Money.ZERO))
}
