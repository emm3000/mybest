package com.emm.mybest.data.mappers

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.WonNight
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

private val SATURDAY = LocalDate(2026, 10, 24)

class WonNightMappersTest {

    @Test
    fun `a won night with an estimated cost round-trips through the entity`() {
        val wonNight = WonNight(appDay = SATURDAY, estimatedCost = Money(9_000))

        assertEquals(wonNight, wonNight.toEntity().toDomain())
    }

    @Test
    fun `a won night with a null cost round-trips through the entity`() {
        val wonNight = WonNight(appDay = SATURDAY, estimatedCost = null)

        assertEquals(wonNight, wonNight.toEntity().toDomain())
    }
}
