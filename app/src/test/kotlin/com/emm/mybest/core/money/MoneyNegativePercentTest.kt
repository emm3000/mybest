package com.emm.mybest.core.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyNegativePercentTest {
    @Test
    fun negativePartIsNegativePercent() {
        assertEquals(-50, Money(-50).percentOf(Money(100)))
    }

    @Test
    fun negativeWholeFlipsSign() {
        assertEquals(-50, Money(50).percentOf(Money(-100)))
    }

    @Test
    fun bothNegativeIsPositivePercent() {
        assertEquals(50, Money(-50).percentOf(Money(-100)))
    }

    @Test
    fun exactNegativePercentIsKept() {
        assertEquals(-23, Money(-4600).percentOf(Money(20000)))
    }

    @Test
    fun negativeHalfRoundsTowardPositiveInfinity() {
        assertEquals(0, Money(-1).percentOf(Money(200)))
        assertEquals(-1, Money(-3).percentOf(Money(200)))
    }

    @Test
    fun negativeBelowHalfRoundsDown() {
        assertEquals(-1, Money(-1).percentOf(Money(199)))
    }

    @Test
    fun resultAtIntMinValueBoundaryIsKept() {
        assertEquals(-2_147_483_647, Money(-2_147_483_647).percentOf(Money(100)))
    }

    @Test
    fun resultBelowIntMinValueClampsToIntMinValue() {
        assertEquals(Int.MIN_VALUE, Money(-2_147_483_649).percentOf(Money(100)))
    }

    @Test
    fun longMinValueOverItselfIsOneHundred() {
        assertEquals(100, Money(Long.MIN_VALUE).percentOf(Money(Long.MIN_VALUE)))
    }

    @Test
    fun longMinValuePartOverLongMaxValueIsMinusOneHundred() {
        assertEquals(-100, Money(Long.MIN_VALUE).percentOf(Money(Long.MAX_VALUE)))
    }

    @Test
    fun longMaxValuePartOverLongMinValueIsMinusOneHundred() {
        assertEquals(-100, Money(Long.MAX_VALUE).percentOf(Money(Long.MIN_VALUE)))
    }

    @Test
    fun longMinValuePartOverOneClampsToIntMinValue() {
        assertEquals(Int.MIN_VALUE, Money(Long.MIN_VALUE).percentOf(Money(1)))
    }

    @Test
    fun onePartOverLongMinValueIsZero() {
        assertEquals(0, Money(1).percentOf(Money(Long.MIN_VALUE)))
    }

    @Test
    fun zeroWholeIsNullForNegativePart() {
        assertNull(Money(-50).percentOf(Money.ZERO))
    }
}
