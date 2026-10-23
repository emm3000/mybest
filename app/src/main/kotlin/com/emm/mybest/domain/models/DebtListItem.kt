package com.emm.mybest.domain.models

/**
 * A [debt] together with whether its installment is already paid for the month of the current app day.
 */
data class DebtListItem(
    val debt: Debt,
    val isPaidThisMonth: Boolean,
)
