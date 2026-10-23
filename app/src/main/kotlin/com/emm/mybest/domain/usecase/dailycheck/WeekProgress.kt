package com.emm.mybest.domain.usecase.dailycheck

/**
 * Days of one daily check counted in an app week against its weekly [target]. For alcohol, [count] is days drank.
 */
data class WeekProgress(
    val count: Int,
    val target: Int,
)
