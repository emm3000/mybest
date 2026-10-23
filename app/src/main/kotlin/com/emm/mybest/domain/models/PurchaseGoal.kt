package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

/**
 * The one durable item the user wants, named [name] and costing [price], funded only by real contributions.
 */
data class PurchaseGoal(
    val name: String,
    val price: Money,
)
