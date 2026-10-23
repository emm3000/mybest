package com.emm.mybest.data.mappers

import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.GoalContributionEntity
import com.emm.mybest.data.entities.PurchaseGoalEntity
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.PurchaseGoal

fun PurchaseGoal.toEntity(): PurchaseGoalEntity = PurchaseGoalEntity(
    name = name,
    priceCents = price.cents,
)

fun PurchaseGoalEntity.toDomain(): PurchaseGoal = PurchaseGoal(
    name = name,
    price = Money(priceCents),
)

fun GoalContribution.toEntity(): GoalContributionEntity = GoalContributionEntity(
    id = id,
    appDay = appDay,
    amountCents = amount.cents,
)

fun GoalContributionEntity.toDomain(): GoalContribution = GoalContribution(
    id = id,
    appDay = appDay,
    amount = Money(amountCents),
)
