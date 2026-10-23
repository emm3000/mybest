package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money

/**
 * The money side of one app week: its [wonNights] with the [notSpentEstimated] total, which is never real money, the
 * [debt] and the purchase [goal] progress, null without a goal.
 */
data class WeekMoney(
    val wonNights: List<WonNight>,
    val notSpentEstimated: Money,
    val debt: WeekDebt,
    val goal: GoalProgress?,
)
