package com.emm.mybest.domain.models

/**
 * An [outing] that broke a limit, exceeded the monthly outing limit or was unplanned, with every one of its [reasons].
 */
data class Relapse(
    val outing: Outing,
    val reasons: Set<RelapseReason>,
)
