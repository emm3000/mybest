package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.WeekProgress

/**
 * One daily check of the current app day with its week count; [progress] is null while the weekly target is unset.
 */
data class DailyCheckItem(
    val type: DailyCheckType,
    val isDone: Boolean,
    val progress: WeekProgress?,
) {
    val isLimit: Boolean get() = type == DailyCheckType.ALCOHOL

    val isOverLimit: Boolean get() = isLimit && (progress?.exceededBy ?: 0) > 0

    val weekTone: WeekTone get() = if (isOverLimit) WeekTone.BROKEN else WeekTone.NEUTRAL
}
