package com.emm.mybest.core.money

/**
 * The mean of [amounts] rounded half-up to the cent, or null when the list is empty.
 */
fun averageOf(amounts: List<Money>): Money? {
    if (amounts.isEmpty()) return null
    val totalCents = amounts.sumOf { it.cents }
    return Money(divideRoundingHalfUp(totalCents, amounts.size.toLong()))
}
