package com.emm.mybest.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightInputValidatorTest {

    @Test
    fun `integer weight is valid`() {
        assertTrue(WeightInputValidator.isValidWeightInput("72"))
    }

    @Test
    fun `decimal weight with dot is valid`() {
        assertTrue(WeightInputValidator.isValidWeightInput("72.4"))
    }

    @Test
    fun `decimal weight with comma is valid`() {
        assertTrue(WeightInputValidator.isValidWeightInput("72,4"))
    }

    @Test
    fun `two decimal places with dot is valid`() {
        assertTrue(WeightInputValidator.isValidWeightInput("72.45"))
    }

    @Test
    fun `two decimal places with comma is valid`() {
        assertTrue(WeightInputValidator.isValidWeightInput("72,45"))
    }

    @Test
    fun `blank input is invalid`() {
        assertFalse(WeightInputValidator.isValidWeightInput(""))
    }

    @Test
    fun `whitespace-only input is invalid`() {
        assertFalse(WeightInputValidator.isValidWeightInput("   "))
    }

    @Test
    fun `alphabetic input is invalid`() {
        assertFalse(WeightInputValidator.isValidWeightInput("abc"))
    }

    @Test
    fun `three decimal places is invalid`() {
        assertFalse(WeightInputValidator.isValidWeightInput("72.456"))
    }

    @Test
    fun `trailing dot only is invalid`() {
        assertFalse(WeightInputValidator.isValidWeightInput("."))
    }

    @Test
    fun `parse dot decimal returns correct float`() {
        assertEquals(72.5f, WeightInputValidator.parse("72.5"))
    }

    @Test
    fun `parse comma decimal normalizes and returns correct float`() {
        assertEquals(72.5f, WeightInputValidator.parse("72,5"))
    }

    @Test
    fun `parse alphabetic returns null`() {
        assertNull(WeightInputValidator.parse("abc"))
    }

    @Test
    fun `parse empty string returns null`() {
        assertNull(WeightInputValidator.parse(""))
    }

    @Test
    fun `isWeightInRange returns true for 20`() {
        assertTrue(WeightInputValidator.isWeightInRange(20f))
    }

    @Test
    fun `isWeightInRange returns true for 500`() {
        assertTrue(WeightInputValidator.isWeightInRange(500f))
    }

    @Test
    fun `isWeightInRange returns false for 19_99`() {
        assertFalse(WeightInputValidator.isWeightInRange(19.99f))
    }

    @Test
    fun `isWeightInRange returns false for 500_01`() {
        assertFalse(WeightInputValidator.isWeightInRange(500.01f))
    }

    @Test
    fun `isWeightInRange returns false for 232323`() {
        assertFalse(WeightInputValidator.isWeightInRange(232323f))
    }
}
