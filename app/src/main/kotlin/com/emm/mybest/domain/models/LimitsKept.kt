package com.emm.mybest.domain.models

/**
 * How many of the [checkedIn] planned outings, those with a check-in, kept every limit.
 */
data class LimitsKept(
    val kept: Int,
    val checkedIn: Int,
)
