package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalDate

/**
 * Real money of [amount] the user put toward the purchase goal on [appDay].
 */
data class GoalContribution(
    val id: Long,
    val appDay: LocalDate,
    val amount: Money,
)
