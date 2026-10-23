package com.emm.mybest.domain.validation

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.PurchaseGoal

/**
 * What a user entered for the purchase goal; a null price means the input was empty or not a number.
 */
data class PurchaseGoalDraft(
    val name: String,
    val price: Money?,
) {
    /**
     * The goal this draft describes, or null when the price is missing. Callers that need to report why run
     * [PurchaseGoalValidator] first.
     */
    fun toGoalOrNull(): PurchaseGoal? = price?.let { PurchaseGoal(name.trim(), it) }
}
