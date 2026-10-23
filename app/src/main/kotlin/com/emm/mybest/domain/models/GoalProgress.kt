package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

/**
 * How far real contributions have funded [goal]: [contributed] money, [left] to go (never below zero) and the whole
 * [percent] funded (0 to 100). [contributions] are listed newest first.
 */
data class GoalProgress(
    val goal: PurchaseGoal,
    val contributed: Money,
    val left: Money,
    val percent: Int,
    val contributions: List<GoalContribution>,
)
