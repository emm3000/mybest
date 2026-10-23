package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

/**
 * The outings of one app week: how many, the [totalSpent] by their check-ins, how many are still pending their
 * check-in and the [limitsKept].
 */
data class WeekOutings(
    val count: Int,
    val totalSpent: Money,
    val pendingCheckIns: Int,
    val limitsKept: LimitsKept,
)
