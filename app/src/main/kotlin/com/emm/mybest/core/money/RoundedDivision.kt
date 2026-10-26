package com.emm.mybest.core.money

private const val HALF_DIVISOR = 2

internal fun divideRoundingHalfUp(dividend: Long, divisor: Long): Long =
    Math.floorDiv(dividend * HALF_DIVISOR + divisor, divisor * HALF_DIVISOR)

private const val PERCENT_SCALE = 100
private const val MAX_WHOLE_SHARE = Int.MAX_VALUE / PERCENT_SCALE

internal fun percentRoundingHalfUp(part: Long, whole: Long): Int {
    val wholeShare = part / whole
    if (wholeShare > MAX_WHOLE_SHARE) return Int.MAX_VALUE
    val percent = wholeShare * PERCENT_SCALE + fractionPercentRoundingHalfUp(part % whole, whole)
    return percent.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

private fun fractionPercentRoundingHalfUp(remainder: Long, whole: Long): Long {
    var carries = 0L
    var accumulated = 0L
    repeat(PERCENT_SCALE) {
        val room = whole - accumulated
        if (remainder >= room) {
            carries++
            accumulated = remainder - room
        } else {
            accumulated += remainder
        }
    }
    return if (accumulated >= whole - accumulated) carries + 1 else carries
}
