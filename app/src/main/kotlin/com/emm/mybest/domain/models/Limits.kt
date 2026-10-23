package com.emm.mybest.domain.models

import com.emm.mybest.core.datetime.minutesIntoAppDay
import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalTime

/**
 * The three caps set before a planned outing: [money], [drinks] and [returnTime].
 */
data class Limits(
    val money: Money,
    val drinks: Int,
    val returnTime: LocalTime,
) {
    /**
     * How far [checkIn] went past each limit. Money is always evaluated; drinks and return time only when the check-in
     * answers them, so a null answer leaves that limit out. Return times are compared on the app-day timeline that
     * starts at 05:00, so 00:15 is later than 23:30.
     */
    fun evaluate(checkIn: CheckIn): List<LimitResult> = listOfNotNull(
        LimitResult(LimitKind.MONEY, excess(checkIn.totalSpent.cents, money.cents)),
        checkIn.drinks?.let { LimitResult(LimitKind.DRINKS, excess(it.toLong(), drinks.toLong())) },
        checkIn.returnTime?.let { LimitResult(LimitKind.RETURN_TIME, returnTimeExcess(it)) },
    )

    private fun returnTimeExcess(actual: LocalTime): Long =
        excess(actual.minutesIntoAppDay().toLong(), returnTime.minutesIntoAppDay().toLong())

    private fun excess(actual: Long, limit: Long): Long = (actual - limit).coerceAtLeast(0)
}
