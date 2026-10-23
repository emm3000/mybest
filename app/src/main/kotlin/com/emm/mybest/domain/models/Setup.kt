package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

/**
 * The user's setup. Every value stays unset until the user chooses it; a missing key in [weeklyTargets] means
 * that daily check has no weekly target yet.
 */
data class Setup(
    val targetWeightKg: Float? = null,
    val monthlyOutingLimit: Int? = null,
    val typicalOutingCost: Money? = null,
    val weeklyTargets: Map<DailyCheckType, Int> = emptyMap(),
    val preOutingReminderTime: LocalTime? = null,
    val checkInReminderTime: LocalTime? = null,
    val weighInDay: DayOfWeek? = null,
    val weighInReminderTime: LocalTime? = null,
    val isPhotoReminderEnabled: Boolean = false,
    val debtReminderTime: LocalTime? = null,
)
