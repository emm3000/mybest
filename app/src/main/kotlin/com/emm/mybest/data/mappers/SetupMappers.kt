package com.emm.mybest.data.mappers

import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.UserSetupEntity
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.validation.WeightInputValidator
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber

fun Setup.toEntity(): UserSetupEntity = UserSetupEntity(
    targetWeightKg = targetWeightKg,
    monthlyOutingLimit = monthlyOutingLimit,
    typicalOutingCostCents = typicalOutingCost?.cents,
    gymWeeklyTarget = weeklyTargets[DailyCheckType.GYM],
    dietWeeklyTarget = weeklyTargets[DailyCheckType.DIET],
    englishWeeklyTarget = weeklyTargets[DailyCheckType.ENGLISH],
    alcoholWeeklyTarget = weeklyTargets[DailyCheckType.ALCOHOL],
    preOutingReminderMinutes = preOutingReminderTime?.toMinutesSinceMidnight(),
    checkInReminderMinutes = checkInReminderTime?.toMinutesSinceMidnight(),
    weighInDay = weighInDay?.isoDayNumber,
    weighInReminderMinutes = weighInReminderTime?.toMinutesSinceMidnight(),
    isPhotoReminderEnabled = isPhotoReminderEnabled,
    debtReminderMinutes = debtReminderTime?.toMinutesSinceMidnight(),
)

fun UserSetupEntity.toDomain(): Setup = Setup(
    targetWeightKg = targetWeightKg?.takeIf(WeightInputValidator::isWeightInRange),
    monthlyOutingLimit = monthlyOutingLimit,
    typicalOutingCost = typicalOutingCostCents?.let(::Money),
    weeklyTargets = weeklyTargets(),
    preOutingReminderTime = preOutingReminderMinutes?.toLocalTime(),
    checkInReminderTime = checkInReminderMinutes?.toLocalTime(),
    weighInDay = weighInDay?.let(::DayOfWeek),
    weighInReminderTime = weighInReminderMinutes?.toLocalTime(),
    isPhotoReminderEnabled = isPhotoReminderEnabled,
    debtReminderTime = debtReminderMinutes?.toLocalTime(),
)

private fun UserSetupEntity.weeklyTargets(): Map<DailyCheckType, Int> = listOfNotNull(
    gymWeeklyTarget?.let { DailyCheckType.GYM to it },
    dietWeeklyTarget?.let { DailyCheckType.DIET to it },
    englishWeeklyTarget?.let { DailyCheckType.ENGLISH to it },
    alcoholWeeklyTarget?.let { DailyCheckType.ALCOHOL to it },
).toMap()
