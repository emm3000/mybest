package com.emm.mybest.domain.models

import com.emm.mybest.core.datetime.YearMonthValue

/**
 * The ids of the debts whose installment is paid in [month].
 */
fun List<DebtPayment>.debtIdsPaidIn(month: YearMonthValue): Set<Long> =
    filter { it.month == month }.mapTo(mutableSetOf()) { it.debtId }

/**
 * Whether the installment of [debt] is paid for [month].
 */
fun List<DebtPayment>.hasPaymentFor(debt: Debt, month: YearMonthValue): Boolean =
    any { it.debtId == debt.id && it.month == month }
