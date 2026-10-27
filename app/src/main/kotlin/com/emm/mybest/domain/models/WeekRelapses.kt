package com.emm.mybest.domain.models

/**
 * The [relapses] of one app week, with the [latestReturnTime] of the newest of those relapses, null when the week has
 * none, and the [returnTimeHistory] of every relapse up to the end of the week, newest first.
 */
data class WeekRelapses(
    val relapses: List<Relapse>,
    val latestReturnTime: ReturnTime?,
    val returnTimeHistory: List<ReturnTime>,
)
