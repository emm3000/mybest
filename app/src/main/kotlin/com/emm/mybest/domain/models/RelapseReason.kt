package com.emm.mybest.domain.models

/**
 * Why an outing is a relapse: it [BROKE_LIMIT], went [OVER_MONTHLY_LIMIT] or was [UNPLANNED].
 */
enum class RelapseReason {
    BROKE_LIMIT,
    OVER_MONTHLY_LIMIT,
    UNPLANNED,
}
