package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val DAY = LocalDate(2026, 10, 23)

class DailyCheckTest {

    @Test
    fun `a done target is kept`() {
        assertTrue(DailyCheck(DAY, DailyCheckType.GYM, isDone = true).isKept())
    }

    @Test
    fun `a target marked not done is not kept`() {
        assertFalse(DailyCheck(DAY, DailyCheckType.DIET, isDone = false).isKept())
    }

    @Test
    fun `alcohol recorded as not drank is kept`() {
        assertTrue(DailyCheck(DAY, DailyCheckType.ALCOHOL, isDone = false).isKept())
    }

    @Test
    fun `alcohol recorded as drank is not kept`() {
        assertFalse(DailyCheck(DAY, DailyCheckType.ALCOHOL, isDone = true).isKept())
    }
}
