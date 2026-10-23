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

        private const val CENT_DIGITS = 2
        private const val MAX_SOLES = Long.MAX_VALUE / CENTS_PER_SOL - 1
        private val INPUT_PATTERN = Regex("""(\d+)(?:[.,](\d{1,2}))?""")

        val ZERO = Money(0)

        /**
         * Parses user input such as `120`, `120.5` or `120,50` into soles, or null when it is not a valid non-negative amount.
         */
        fun parseOrNull(input: String): Money? {
            val match = INPUT_PATTERN.matchEntire(input.trim()) ?: return null
            val soles = match.groupValues[1].toLongOrNull() ?: return null
            val fraction = match.groupValues[2].padEnd(CENT_DIGITS, '0').toLong()
            return centsOrNull(soles, fraction)
        }

        private fun centsOrNull(soles: Long, fraction: Long): Money? =
            if (soles > MAX_SOLES) null else Money(soles * CENTS_PER_SOL + fraction)
    }
}
