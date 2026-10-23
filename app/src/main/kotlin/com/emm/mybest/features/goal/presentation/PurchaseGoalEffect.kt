package com.emm.mybest.features.goal.presentation

sealed interface PurchaseGoalEffect {
    data object ActionFailed : PurchaseGoalEffect
}
