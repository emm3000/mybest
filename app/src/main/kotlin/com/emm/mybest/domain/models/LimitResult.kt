package com.emm.mybest.domain.models

/**
 * How far an outing went past one limit of [kind]: cents for money, drinks for drinks and minutes for return time.
 * Zero means the limit was kept.
 */
data class LimitResult(
    val kind: LimitKind,
    val exceededBy: Long,
)
