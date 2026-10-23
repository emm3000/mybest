package com.emm.mybest.data.mappers

import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.OutingEntity
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing

fun Outing.toEntity(): OutingEntity = OutingEntity(
    id = id,
    appDay = appDay,
    kind = kind,
    limitMoneyCents = limits?.money?.cents,
    limitDrinks = limits?.drinks,
    limitReturnMinutes = limits?.returnTime?.toMinutesSinceMidnight(),
    spentCents = checkIn?.totalSpent?.cents,
    drinks = checkIn?.drinks,
    returnMinutes = checkIn?.returnTime?.toMinutesSinceMidnight(),
    mood = checkIn?.mood,
)

fun OutingEntity.toDomain(): Outing = Outing(
    id = id,
    appDay = appDay,
    kind = kind,
    limits = limitsOrNull(),
    checkIn = checkInOrNull(),
)

private fun OutingEntity.limitsOrNull(): Limits? {
    if (limitMoneyCents == null || limitDrinks == null || limitReturnMinutes == null) return null
    return Limits(
        money = Money(limitMoneyCents),
        drinks = limitDrinks,
        returnTime = limitReturnMinutes.toLocalTime(),
    )
}

private fun OutingEntity.checkInOrNull(): CheckIn? {
    if (spentCents == null || mood == null) return null
    return CheckIn(
        totalSpent = Money(spentCents),
        drinks = drinks,
        returnTime = returnMinutes?.toLocalTime(),
        mood = mood,
    )
}
