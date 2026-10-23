package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

/**
 * The estimated outing [cost], or null while neither a typical cost nor enough outings exist. [isAveraged] is true when
 * the cost is the average of past outings rather than the typical cost declared in setup.
 */
data class OutingCostEstimate(
    val cost: Money?,
    val isAveraged: Boolean,
)
