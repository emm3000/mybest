package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

/**
 * The current [appDay] with its [decision]. [canStayIn] is true only on Friday and Saturday app days, the only days a
 * won night can be recorded.
 */
data class Tonight(
    val appDay: LocalDate,
    val decision: NightDecision,
    val canStayIn: Boolean,
)
