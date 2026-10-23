package com.emm.mybest.features.goal.presentation

data class PurchaseGoalState(
    val isLoaded: Boolean = false,
    val goal: GoalSummaryState? = null,
    val notSpentLabel: String = "",
    val goalForm: GoalFormState = GoalFormState(),
    val contributionForm: ContributionFormState? = null,
    val isClearConfirmationVisible: Boolean = false,
)
