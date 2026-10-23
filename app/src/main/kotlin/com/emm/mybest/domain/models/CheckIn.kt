package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalTime

/**
 * The next-morning record of an outing. [drinks] and [returnTime] are null when the user did not answer them; the
 * matching limit is then not evaluated.
 */
data class CheckIn(
    val totalSpent: Money,
    val drinks: Int?,
    val returnTime: LocalTime?,
    val mood: Int,
)
