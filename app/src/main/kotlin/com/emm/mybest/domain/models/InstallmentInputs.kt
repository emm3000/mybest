package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalDate

/**
 * The debts and installment payments stored on [today], from which the pre-outing debt facts are derived.
 */
data class InstallmentInputs(
    val debts: List<Debt>,
    val payments: List<DebtPayment>,
    val today: LocalDate,
) {
    val hasDebts: Boolean get() = debts.isNotEmpty()

    /**
     * Returns the installment with the earliest payment date on or after [today] among the debts with a balance, with
     * its share of [estimate], or null without them. A debt's installment falls on its due month, or one month later
     * when that month is already paid; a payment day past the end of a month falls on its last day.
     */
    fun nearestShare(estimate: Money?): InstallmentShare? =
        nearestDue(debts.filter { it.hasBalance }, payments, today)?.toShare(estimate)
}

private fun nearestDue(debts: List<Debt>, payments: List<DebtPayment>, today: LocalDate): Pair<Debt, LocalDate>? =
    debts.map { it to nextPaymentDate(it, payments, today) }.minByOrNull { it.second }

private fun Pair<Debt, LocalDate>.toShare(estimate: Money?): InstallmentShare =
    InstallmentShare(first.name, second, first.installment, estimate?.percentOf(first.installment))

private fun nextPaymentDate(debt: Debt, payments: List<DebtPayment>, today: LocalDate): LocalDate {
    val dueMonth = debt.dueMonthOn(today)
    return debt.paymentDateIn(if (payments.hasPaymentFor(debt, dueMonth)) dueMonth.plusMonths(1) else dueMonth)
}
