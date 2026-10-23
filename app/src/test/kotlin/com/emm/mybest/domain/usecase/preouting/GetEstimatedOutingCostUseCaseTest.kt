package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakeSetupRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val TYPICAL_COST = Money(380_00)

class GetEstimatedOutingCostUseCaseTest {

    private fun outing(day: Int, spent: Money?) = Outing(
        id = day.toLong(),
        appDay = LocalDate(2026, 9, day),
        kind = OutingKind.UNPLANNED,
        limits = null,
        checkIn = spent?.let { CheckIn(totalSpent = it, drinks = null, returnTime = null, mood = 3) },
    )

    private fun useCase(setup: Setup, outings: List<Outing>) = GetEstimatedOutingCostUseCase(
        setupRepository = FakeSetupRepository(setup),
        outingRepository = FakeOutingRepository(outings),
    )

    @Test
    fun `is null without a typical cost and fewer than three outings`() = runTest {
        val estimate = useCase(Setup(), listOf(outing(4, Money(100_00)))).invoke().first()

        assertNull(estimate)
    }

    @Test
    fun `uses the typical cost with fewer than three outings`() = runTest {
        val outings = listOf(outing(4, Money(100_00)), outing(11, Money(200_00)))

        val estimate = useCase(Setup(typicalOutingCost = TYPICAL_COST), outings).invoke().first()

        assertEquals(TYPICAL_COST, estimate)
    }

    @Test
    fun `averages the total spent once three outings have a check-in`() = runTest {
        val outings = listOf(outing(4, Money(100_00)), outing(11, Money(200_00)), outing(18, Money(450_00)))

        val estimate = useCase(Setup(typicalOutingCost = TYPICAL_COST), outings).invoke().first()

        assertEquals(Money(250_00), estimate)
    }

    @Test
    fun `averages without a typical cost once three outings exist`() = runTest {
        val outings = listOf(outing(4, Money(100_00)), outing(11, Money(100_00)), outing(18, Money(100_00)))

        val estimate = useCase(Setup(), outings).invoke().first()

        assertEquals(Money(100_00), estimate)
    }

    @Test
    fun `ignores outings pending their check-in`() = runTest {
        val outings = listOf(
            outing(4, Money(100_00)),
            outing(11, Money(200_00)),
            outing(18, spent = null),
            outing(25, spent = null),
        )

        val estimate = useCase(Setup(typicalOutingCost = TYPICAL_COST), outings).invoke().first()

        assertEquals(TYPICAL_COST, estimate)
    }
}
