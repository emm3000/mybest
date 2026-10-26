package com.emm.mybest.features.goal.presentation

sealed interface PurchaseGoalIntent {
    sealed interface GoalIntent : PurchaseGoalIntent
    sealed interface ContributionIntent : PurchaseGoalIntent
    sealed interface ClearIntent : PurchaseGoalIntent

    data class OnGoalChange(val field: GoalFormField, val value: String) : GoalIntent
    data object OnGoalSave : GoalIntent
    data object OnEditClick : GoalIntent
    data object OnEditCancel : GoalIntent
    data object OnAddContributionClick : ContributionIntent
    data class OnContributionChange(val value: String) : ContributionIntent
    data object OnContributionSave : ContributionIntent
    data object OnContributionDismiss : ContributionIntent
    data class OnContributionDelete(val id: Long) : ContributionIntent
    data object OnClearClick : ClearIntent
    data object OnClearConfirm : ClearIntent
    data object OnClearDismiss : ClearIntent
}
