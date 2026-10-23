package com.emm.mybest.domain.models

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money

/**
 * The installment of the debt with [debtId] marked paid for [month], lowering its balance by [amount].
 */
data class DebtPayment(
    val debtId: Long,
    val month: YearMonthValue,
    val amount: Money,
)
