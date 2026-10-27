package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

private val OVERNIGHT_LIMITS = Limits(money = Money(12_000), drinks = 4, returnTime = LocalTime(2, 0))
private val EVENING_LIMITS = OVERNIGHT_LIMITS.copy(returnTime = LocalTime(23, 0))
private val LATE_EVENING_LIMITS = OVERNIGHT_LIMITS.copy(returnTime = LocalTime(23, 30))
private val EVENING_START_LIMITS = OVERNIGHT_LIMITS.copy(returnTime = LocalTime(18, 0))
private val MIDNIGHT_LIMITS = OVERNIGHT_LIMITS.copy(returnTime = LocalTime(0, 0))
private val LAST_DAWN_MINUTE_LIMITS = OVERNIGHT_LIMITS.copy(returnTime = LocalTime(4, 59))

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

    @Test
    fun `returning at 02_30 against a 02_00 limit breaks it by 30 minutes`() {
        assertEquals(30L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(2, 30)))
    }

    @Test
    fun `returning at 07_00 against a 02_00 limit breaks it by 300 minutes`() {
        assertEquals(300L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(7, 0)))
    }

    @Test
    fun `returning at 12_30 against a 02_00 limit breaks it by 630 minutes the next day`() {
        assertEquals(630L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(12, 30)))
    }

    @Test
    fun `returning at 17_59 against a 02_00 limit breaks it by 959 minutes the next day`() {
        assertEquals(959L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(17, 59)))
    }

    @Test
    fun `returning at 18_00 against a 02_00 limit keeps the return time limit the same evening`() {
        assertEquals(0L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(18, 0)))
    }

    @Test
    fun `returning at 23_00 against a 02_00 limit keeps the return time limit the same evening`() {
        assertEquals(0L, returnTimeExcess(OVERNIGHT_LIMITS, LocalTime(23, 0)))
    }

    @Test
    fun `returning at 00_15 against a 23_30 limit breaks it by 45 minutes across midnight`() {
        assertEquals(45L, returnTimeExcess(LATE_EVENING_LIMITS, LocalTime(0, 15)))
    }

    @Test
    fun `returning at 23_00 against a 23_30 limit keeps the return time limit`() {
        assertEquals(0L, returnTimeExcess(LATE_EVENING_LIMITS, LocalTime(23, 0)))
    }

    @Test
    fun `returning at 18_00 against a 18_00 limit keeps the return time limit on the evening timeline`() {
        assertEquals(0L, returnTimeExcess(EVENING_START_LIMITS, LocalTime(18, 0)))
    }

    @Test
    fun `returning at 18_01 against a 18_00 limit breaks it by 1 minutes on the evening timeline`() {
        assertEquals(1L, returnTimeExcess(EVENING_START_LIMITS, LocalTime(18, 1)))
    }

    @Test
    fun `returning at 00_00 against a 18_00 limit breaks it by 360 minutes on the evening timeline`() {
        assertEquals(360L, returnTimeExcess(EVENING_START_LIMITS, LocalTime(0, 0)))
    }

    @Test
    fun `returning at 17_59 against a 18_00 limit breaks it by 1439 minutes on the evening timeline`() {
        assertEquals(1439L, returnTimeExcess(EVENING_START_LIMITS, LocalTime(17, 59)))
    }

    @Test
    fun `returning at 23_59 against a 00_00 limit keeps the return time limit on the evening timeline`() {
        assertEquals(0L, returnTimeExcess(MIDNIGHT_LIMITS, LocalTime(23, 59)))
    }

    @Test
    fun `returning at 00_00 against a 00_00 limit keeps the return time limit on the evening timeline`() {
        assertEquals(0L, returnTimeExcess(MIDNIGHT_LIMITS, LocalTime(0, 0)))
    }

    @Test
    fun `returning at 00_01 against a 00_00 limit breaks it by 1 minutes on the evening timeline`() {
        assertEquals(1L, returnTimeExcess(MIDNIGHT_LIMITS, LocalTime(0, 1)))
    }

    @Test
    fun `returning at 05_00 against a 00_00 limit breaks it by 300 minutes on the evening timeline`() {
        assertEquals(300L, returnTimeExcess(MIDNIGHT_LIMITS, LocalTime(5, 0)))
    }

    @Test
    fun `returning at 17_59 against a 00_00 limit breaks it by 1079 minutes on the evening timeline`() {
        assertEquals(1079L, returnTimeExcess(MIDNIGHT_LIMITS, LocalTime(17, 59)))
    }

    @Test
    fun `returning at 17_59 against a 23_30 limit breaks it by 1109 minutes on the evening timeline`() {
        assertEquals(1109L, returnTimeExcess(LATE_EVENING_LIMITS, LocalTime(17, 59)))
    }

    @Test
    fun `returning at 18_00 against a 23_30 limit keeps the return time limit on the evening timeline`() {
        assertEquals(0L, returnTimeExcess(LATE_EVENING_LIMITS, LocalTime(18, 0)))
    }

    @Test
    fun `returning at 04_59 against a 04_59 limit keeps the return time limit on the evening timeline`() {
        assertEquals(0L, returnTimeExcess(LAST_DAWN_MINUTE_LIMITS, LocalTime(4, 59)))
    }

    @Test
    fun `returning at 05_00 against a 04_59 limit breaks it by 1 minutes on the evening timeline`() {
        assertEquals(1L, returnTimeExcess(LAST_DAWN_MINUTE_LIMITS, LocalTime(5, 0)))
    }
}
