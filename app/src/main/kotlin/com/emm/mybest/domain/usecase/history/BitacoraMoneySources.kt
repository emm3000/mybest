package com.emm.mybest.domain.usecase.history

import com.emm.mybest.domain.repository.DebtRepository
import com.emm.mybest.domain.repository.PurchaseGoalRepository

data class BitacoraMoneySources(
    val debtRepository: DebtRepository,
    val purchaseGoalRepository: PurchaseGoalRepository,
)
