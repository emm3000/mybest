package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.money.Money

/**
 * What happened when the user tried to record a won night.
 */
sealed interface RecordWonNightResult {
    /**
     * The night was recorded with [estimatedCost], or null when no estimate was known.
     */
    data class Recorded(val estimatedCost: Money?) : RecordWonNightResult
    data object NotWonNightDay : RecordWonNightResult
    data object DayAlreadyDecided : RecordWonNightResult
}
