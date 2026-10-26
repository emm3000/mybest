package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

/**
 * Every money number shown before the decision to go out. [estimatedOutingCost] (an average of past outings when
 * [isEstimateAveraged], else the typical cost declared in setup), [nearestInstallment], [purchaseGoalLeft] and
 * [monthlyOutingLimit] are null while the user has not set the input they come from. [hasDebts] is true once any
 * debt exists, even when every balance is paid off and [nearestInstallment] is null. [notSpentThisMonth] is an
 * estimate and never real money.
 */
data class PreOutingSnapshot(
    val spentOnOutingsThisMonth: Money,
    val outingsThisMonth: Int,
    val monthlyOutingLimit: Int?,
    val isNextOutingOverLimit: Boolean,
    val estimatedOutingCost: Money?,
    val isEstimateAveraged: Boolean,
    val hasDebts: Boolean,
    val nearestInstallment: InstallmentShare?,
    val purchaseGoalLeft: Money?,
    val notSpentThisMonth: Money,
)
