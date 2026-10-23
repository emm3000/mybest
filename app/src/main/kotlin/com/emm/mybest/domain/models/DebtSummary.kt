package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

/**
 * How many debts exist and the sum of their balances.
 */
data class DebtSummary(
    val count: Int,
    val totalBalance: Money,
)
