package com.emm.mybest.core.money

import java.math.BigInteger

private const val HALF_DIVISOR = 2

internal fun divideRoundingHalfUp(dividend: Long, divisor: Long): Long =
    Math.floorDiv(dividend * HALF_DIVISOR + divisor, divisor * HALF_DIVISOR)

private const val PERCENT_SCALE = 100L
private val PERCENT_SCALE_BIG = BigInteger.valueOf(PERCENT_SCALE)
private val HALF_DIVISOR_BIG = BigInteger.valueOf(HALF_DIVISOR.toLong())
private val INT_MIN_BIG = BigInteger.valueOf(Int.MIN_VALUE.toLong())
private val INT_MAX_BIG = BigInteger.valueOf(Int.MAX_VALUE.toLong())

internal fun percentRoundingHalfUp(part: Long, whole: Long): Int {
    val sign = if (whole < 0) BigInteger.ONE.negate() else BigInteger.ONE
    val signedPart = BigInteger.valueOf(part) * sign
    val signedWhole = BigInteger.valueOf(whole) * sign
    val numerator = signedPart * PERCENT_SCALE_BIG * HALF_DIVISOR_BIG + signedWhole
    val denominator = signedWhole * HALF_DIVISOR_BIG
    val floored = (numerator - numerator.mod(denominator)) / denominator
    return floored.coerceIn(INT_MIN_BIG, INT_MAX_BIG).toInt()
}
