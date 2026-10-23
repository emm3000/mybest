package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

/**
 * What the current month holds in outings: [spent] in checked-in outings, the [count] of outings of any kind and the
 * [limit] set for the month, or null when none is set.
 */
data class OutingMonth(
    val spent: Money,
    val count: Int,
    val limit: Int?,
) {
    val isNextOutingOverLimit: Boolean get() = limit != null && count + 1 > limit
}
