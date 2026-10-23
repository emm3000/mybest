package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.usecase.dailycheck.WeekProgress

/**
 * One daily check of the current app day with its week count; [progress] is null while the weekly target is unset.
 */
data class DailyCheckItem(
    val type: DailyCheckType,
    val isDone: Boolean,
    val progress: WeekProgress?,
)
