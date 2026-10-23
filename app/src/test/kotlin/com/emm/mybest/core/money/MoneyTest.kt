package com.emm.mybest.core.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {
    @Test
    fun plusAddsCents() {
        assertEquals(Money(350), Money(100) + Money(250))
    }

    @Test
    fun minusSubtractsCents() {
        assertEquals(Money(-150), Money(100) - Money(250))
    }

    @Test
    fun timesMultipliesCentsByCount() {
        assertEquals(Money(750), Money(250) * 3)
    }

    @Test
    fun compareToOrdersByCents() {
        assertTrue(Money(100) < Money(250))
        assertTrue(Money(250) > Money(100))
    }

    @Test
    fun isNegativeIsTrueOnlyBelowZero() {
        assertTrue(Money(-1).isNegative)
        assertFalse(Money(0).isNegative)
        assertFalse(Money(1).isNegative)
    }

    @Test
    fun zeroHasNoCents() {
        assertEquals(0L, Money.ZERO.cents)
    }

    @Test
    fun formatPenGroupsThousandsWithTwoDecimals() {
        assertEquals("S/ 1,234.50", Money(123450).formatPen())
    }

    @Test
    fun formatPenShowsZero() {
        assertEquals("S/ 0.00", Money(0).formatPen())
    }

    @Test
    fun formatPenPutsSignBeforeSymbol() {
        assertEquals("-S/ 5.00", Money(-500).formatPen())
    }

    @Test
    fun parseOrNullReadsWholeSoles() {
        assertEquals(Money(12000), Money.parseOrNull("120"))
    }

    @Test
    fun parseOrNullReadsDotDecimals() {
        assertEquals(Money(12050), Money.parseOrNull("120.5"))
    }

    @Test
    fun parseOrNullReadsCommaDecimals() {
        assertEquals(Money(12050), Money.parseOrNull("120,50"))
    }

    @Test
    fun parseOrNullIgnoresSurroundingSpaces() {
        assertEquals(Money(12000), Money.parseOrNull(" 120 "))
    }

    @Test
    fun parseOrNullRejectsInvalidInput() {
        assertNull(Money.parseOrNull(""))
        assertNull(Money.parseOrNull("abc"))
        assertNull(Money.parseOrNull("-5"))
        assertNull(Money.parseOrNull("12.345"))
    }

    @Test
    fun percentOfReturnsRoundedShareOfWhole() {
        assertEquals(23, Money(4600).percentOf(Money(20000)))
    }

    @Test
    fun percentOfRoundsHalfUp() {
        assertEquals(1, Money(1).percentOf(Money(200)))
        assertEquals(0, Money(1).percentOf(Money(201)))
    }

    @Test
    fun percentOfIsNullWhenWholeIsZero() {
        assertNull(Money(4600).percentOf(Money.ZERO))
    }

    @Test
    fun averageOfReturnsMeanOfAmounts() {
        assertEquals(Money(200), averageOf(listOf(Money(100), Money(300))))
    }

    @Test
    fun averageOfRoundsHalfUpToTheCent() {
        assertEquals(Money(2), averageOf(listOf(Money(1), Money(2))))
        assertEquals(Money(33), averageOf(listOf(Money(33), Money(33), Money(34))))
    }

    @Test
    fun averageOfIsNullForEmptyList() {
        assertNull(averageOf(emptyList()))
    }
}
