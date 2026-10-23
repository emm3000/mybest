package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalDate

/**
 * A Friday or Saturday evening on [appDay] where the user chose not to go out. [estimatedCost] is the estimated outing
 * cost at that moment, or null when none could be estimated.
 */
data class WonNight(
    val appDay: LocalDate,
    val estimatedCost: Money?,
)
