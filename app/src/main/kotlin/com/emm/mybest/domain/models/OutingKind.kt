package com.emm.mybest.domain.models

/**
 * How an outing was recorded: [PLANNED] through the pre-outing screen with limits, or [UNPLANNED] when it is first
 * reported at the next-day check-in.
 */
enum class OutingKind {
    PLANNED,
    UNPLANNED,
}
