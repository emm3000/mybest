package com.emm.mybest.domain.models

import com.emm.mybest.core.datetime.YearMonthValue

/**
 * The ids of the debts whose installment is paid in [month].
 */
fun List<DebtPayment>.debtIdsPaidIn(month: YearMonthValue): Set<Long> =
    filter { it.month == month }.mapTo(mutableSetOf()) { it.debtId }
