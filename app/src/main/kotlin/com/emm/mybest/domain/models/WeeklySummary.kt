package com.emm.mybest.domain.models

/**
 * Everything the weekly summary shows for one Monday to Sunday app week. [checks] holds the daily checks that have a
 * weekly target; the [weighInTrend] and the goal progress in [money] are as of the week's end, while the debt
 * balance in [money] is today's.
 */
data class WeeklySummary(
    val outings: WeekOutings,
    val recovery: WeekRecovery,
    val checks: Map<DailyCheckType, WeekProgress>,
    val weighInTrend: WeighInTrend,
    val money: WeekMoney,
)
