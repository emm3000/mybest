package com.emm.mybest.features.preouting.presentation

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.InstallmentShare
import com.emm.mybest.domain.models.PreOutingSnapshot
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private fun snapshotWithShare(nightPercent: Int?) = PreOutingSnapshot(
    spentOnOutingsThisMonth = Money.ZERO,
    outingsThisMonth = 0,
    monthlyOutingLimit = null,
    isNextOutingOverLimit = false,
    estimatedOutingCost = Money(100_00),
    isEstimateAveraged = false,
    hasDebts = true,
    nearestInstallment = InstallmentShare("Grande", LocalDate(2026, 11, 15), Money(400_00), nightPercent),
    purchaseGoalLeft = null,
    notSpentThisMonth = Money.ZERO,
)

class PreOutingStateMappingTest {

    @Test
    fun `a share of 999 shows the normal row`() {
        val state = snapshotWithShare(999).toState(goalName = null)

        assertEquals(999, state.nightPercent)
        assertFalse(state.isNightPercentCapped)
    }

    @Test
    fun `a share of 1000 shows the capped row`() {
        assertTrue(snapshotWithShare(1000).toState(goalName = null).isNightPercentCapped)
    }

    @Test
    fun `a share of Int MAX_VALUE shows the capped row`() {
        assertTrue(snapshotWithShare(Int.MAX_VALUE).toState(goalName = null).isNightPercentCapped)
    }

    @Test
    fun `no share is never capped`() {
        assertFalse(snapshotWithShare(null).toState(goalName = null).isNightPercentCapped)
    }
}
