package com.emm.mybest.features.history.presentation

import androidx.annotation.StringRes

/**
 * Copy of a recent row: the optional [weight] and the string resources of the photo type labels.
 */
data class HistoryRecentDescription(
    val weight: Float?,
    @StringRes val photoLabelResIds: List<Int>,
)
