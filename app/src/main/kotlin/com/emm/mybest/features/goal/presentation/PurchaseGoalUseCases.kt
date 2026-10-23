package com.emm.mybest.features.goal.presentation

import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.usecase.goal.AddGoalContributionUseCase
import com.emm.mybest.domain.usecase.goal.ObserveGoalProgressUseCase
import com.emm.mybest.domain.usecase.goal.SavePurchaseGoalUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveNotSpentThisMonthUseCase

data class PurchaseGoalUseCases(
    val observeGoalProgress: ObserveGoalProgressUseCase,
    val observeNotSpent: ObserveNotSpentThisMonthUseCase,
    val saveGoal: SavePurchaseGoalUseCase,
    val addContribution: AddGoalContributionUseCase,
    val purchaseGoalRepository: PurchaseGoalRepository,
)
