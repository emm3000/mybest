package com.emm.mybest.data.mappers

import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.WonNightEntity
import com.emm.mybest.domain.models.WonNight

fun WonNight.toEntity(): WonNightEntity = WonNightEntity(
    appDay = appDay,
    estimatedCostCents = estimatedCost?.cents,
)

fun WonNightEntity.toDomain(): WonNight = WonNight(
    appDay = appDay,
    estimatedCost = estimatedCostCents?.let(::Money),
)
