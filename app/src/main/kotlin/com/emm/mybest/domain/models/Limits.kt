package com.emm.mybest.domain.models

import com.emm.mybest.core.datetime.minutesIntoEvening
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
     * answers them, so a null answer leaves that limit out. Return times are compared on the evening timeline that
     * starts at 18:00: a time from 18:00 to 23:59 belongs to the outing's evening and a time from 00:00 to 17:59 is the
     * next day, so 00:15 is later than 23:30 and 12:30 is past a 02:00 limit.
     */
    fun evaluate(checkIn: CheckIn): List<LimitResult> = listOfNotNull(
        LimitResult(LimitKind.MONEY, excess(checkIn.totalSpent.cents, money.cents)),
        checkIn.drinks?.let { LimitResult(LimitKind.DRINKS, excess(it.toLong(), drinks.toLong())) },
        checkIn.returnTime?.let { LimitResult(LimitKind.RETURN_TIME, returnTimeExcess(it)) },
    )

    private fun returnTimeExcess(actual: LocalTime): Long =
        excess(actual.minutesIntoEvening().toLong(), returnTime.minutesIntoEvening().toLong())

    private fun excess(actual: Long, limit: Long): Long = (actual - limit).coerceAtLeast(0)
}
