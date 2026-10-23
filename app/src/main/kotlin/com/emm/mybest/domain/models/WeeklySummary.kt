package com.emm.mybest.domain.models

/**
 * Everything the weekly summary shows for one Monday to Sunday app week.
 */
data class WeeklySummary(
    val outings: WeekOutings,
    val recovery: WeekRecovery,
)
