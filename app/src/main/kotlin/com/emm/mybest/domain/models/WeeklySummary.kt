package com.emm.mybest.domain.models

import com.emm.mybest.domain.usecase.dailycheck.WeekProgress

/**
 * Everything the weekly summary shows for one Monday to Sunday app week. [checks] holds the daily checks that have a
 * weekly target; the [weighInTrend] is the latest one, not limited to the week.
 */
data class WeeklySummary(
    val outings: WeekOutings,
    val recovery: WeekRecovery,
    val checks: Map<DailyCheckType, WeekProgress>,
    val weighInTrend: WeighInTrend,
    val money: WeekMoney,
)
