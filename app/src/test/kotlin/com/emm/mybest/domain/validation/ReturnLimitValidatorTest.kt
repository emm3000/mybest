package com.emm.mybest.domain.validation

import kotlinx.datetime.LocalTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReturnLimitValidatorTest {

    @Test
    fun `the evening and the small hours are allowed`() {
        listOf(LocalTime(18, 0), LocalTime(23, 59), LocalTime(0, 0), LocalTime(2, 0), LocalTime(4, 59)).forEach {
            assertTrue("$it", ReturnLimitValidator.isAllowed(it))
        }
    }

    @Test
    fun `the daytime from 05_00 to 17_59 is not allowed`() {
        listOf(LocalTime(5, 0), LocalTime(12, 0), LocalTime(15, 0), LocalTime(17, 59)).forEach {
            assertFalse("$it", ReturnLimitValidator.isAllowed(it))
        }
    }
}
