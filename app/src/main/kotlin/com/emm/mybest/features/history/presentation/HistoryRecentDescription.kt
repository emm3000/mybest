package com.emm.mybest.features.history.presentation

/**
 * Copy of a recent row: the optional [weight], the distinct photo type labels and the
 * [photoCount] of photos that day.
 */
data class HistoryRecentDescription(
    val weight: Float?,
    val photoLabelResIds: List<Int>,
    val photoCount: Int,
)
