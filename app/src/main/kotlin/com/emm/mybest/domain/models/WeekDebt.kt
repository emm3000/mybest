package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

/**
 * The [totalBalance] still owed today, whatever the week, across [debtCount] debts and how many of their
 * installments are paid this month.
 */
data class WeekDebt(
    val totalBalance: Money,
    val installmentsPaidThisMonth: Int,
    val debtCount: Int,
)
