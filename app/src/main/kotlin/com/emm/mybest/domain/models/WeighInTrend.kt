package com.emm.mybest.domain.models

data class WeighInTrend(
    val latestKg: Float? = null,
    val changeSincePreviousKg: Float? = null,
    val remainingToTargetKg: Float? = null,
) {
    val direction: WeighInDirection?
        get() = changeSincePreviousKg?.let {
            when {
                it > 0f -> WeighInDirection.UP
                it < 0f -> WeighInDirection.DOWN
                else -> WeighInDirection.UNCHANGED
            }
        }
}
