package com.emm.mybest.domain.models

/**
 * A [debt] together with whether the installment due on the current app day is already paid.
 */
data class DebtListItem(
    val debt: Debt,
    val isPaidThisMonth: Boolean,
)
