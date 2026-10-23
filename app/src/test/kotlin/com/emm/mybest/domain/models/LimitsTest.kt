package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

private val OVERNIGHT_LIMITS = Limits(money = Money(12_000), drinks = 4, returnTime = LocalTime(2, 0))
private val EVENING_LIMITS = OVERNIGHT_LIMITS.copy(returnTime = LocalTime(23, 0))

private fun returnTimeExcess(limits: Limits, returnTime: LocalTime): Long? {
    val checkIn = CheckIn(totalSpent = Money(0), drinks = null, returnTime = returnTime, mood = 3)
    return limits.evaluate(checkIn).firstOrNull { it.kind == LimitKind.RETURN_TIME }?.exceededBy
}

class LimitsTest {

    @Test
    fun `returning at 01_30 against a 02_00 limit keeps the return time limit`() {
        assertEquals(0L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(1, 30)))
    }

    @Test
    fun `returning at 02_00 against a 02_00 limit keeps the return time limit`() {
        assertEquals(0L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(2, 0)))
    }

    @Test
    fun `returning at 03_15 against a 02_00 limit breaks it by 75 minutes`() {
        assertEquals(75L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(3, 15)))
    }

    @Test
    fun `returning at 05_00 against a 02_00 limit breaks it by 180 minutes`() {
        assertEquals(180L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(5, 0)))
    }

    @Test
    fun `returning at 06_00 against a 02_00 limit breaks it by 240 minutes`() {
        assertEquals(240L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(6, 0)))
    }

    @Test
    fun `returning at 11_59 against a 02_00 limit breaks it by 599 minutes`() {
        assertEquals(599L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(11, 59)))
    }

    @Test
    fun `returning at 23_30 against a 02_00 limit keeps the return time limit`() {
        assertEquals(0L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(23, 30)))
    }

    @Test
    fun `returning at 00_15 against a 23_00 limit breaks it by 75 minutes across midnight`() {
        assertEquals(75L, returnTimeExcess(EVENING_LIMITS, LocalTime(0, 15)))
    }
}
