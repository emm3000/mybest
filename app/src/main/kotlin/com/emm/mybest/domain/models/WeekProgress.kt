package com.emm.mybest.domain.models

/**
 * Days of one daily check counted in an app week against its weekly [target]. For alcohol, [count] is days drank.
 */
data class WeekProgress(
    val count: Int,
    val target: Int,
) {
    /**
     * Days counted past the [target], or zero while within it.
     */
    val exceededBy: Int get() = maxOf(count - target, 0)
}
