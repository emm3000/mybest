package com.emm.mybest.features.goal.presentation

data class GoalSummaryState(
    val name: String,
    val nameLabel: String,
    val priceLabel: String,
    val contributedLabel: String,
    val leftLabel: String,
    val percent: Int,
    val progressFraction: Float,
    val contributions: List<ContributionItemState>,
)
