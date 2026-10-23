package com.emm.mybest.core.money

private const val HALF_DIVISOR = 2

internal fun divideRoundingHalfUp(dividend: Long, divisor: Long): Long =
    Math.floorDiv(dividend * HALF_DIVISOR + divisor, divisor * HALF_DIVISOR)
