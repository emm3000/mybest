package com.emm.mybest.domain.models

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalDate

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

    /**
     * What the next installment asks for: the installment, or the balance left when it is smaller.
     */
    val nextInstallment: Money get() = minOf(installment, balance)

    /**
     * The date this debt's installment falls due in [month]; a payment day past the end of the month falls on its
     * last day.
     */
    fun paymentDateIn(month: YearMonthValue): LocalDate = month.atDay(minOf(paymentDay, month.lengthOfMonth()))

    /**
     * The month of the installment due on app day [today]: the current month while its payment date has not passed,
     * otherwise the next one.
     */
    fun dueMonthOn(today: LocalDate): YearMonthValue {
        val month = YearMonthValue.from(today)
        return if (paymentDateIn(month) >= today) month else month.plusMonths(1)
    }
}
