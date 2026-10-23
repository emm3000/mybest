package com.emm.mybest.core.money

/**
 * An amount of soles (PEN) stored as integer céntimos, so no floating point is involved.
 */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {
    val isNegative: Boolean get() = cents < 0

    operator fun plus(other: Money): Money = Money(cents + other.cents)

    operator fun minus(other: Money): Money = Money(cents - other.cents)

    operator fun times(count: Int): Money = Money(cents * count)

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    companion object {
        const val CENTS_PER_SOL = 100L

        val ZERO = Money(0)
    }
}
