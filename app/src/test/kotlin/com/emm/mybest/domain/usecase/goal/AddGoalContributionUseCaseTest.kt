package com.emm.mybest.domain.usecase.goal

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.validation.ContributionValidationError
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")

class AddGoalContributionUseCaseTest {

    private val repository = FakePurchaseGoalRepository()

    private fun useCase(hour: Int) = AddGoalContributionUseCase(
        purchaseGoalRepository = repository,
        appDayClock = AppDayClock(FixedClock(LocalDateTime(2026, 10, 23, hour, 0).toInstant(LIMA)), LIMA),
    )

    @Test
    fun `a positive amount is stored on the current app day`() = runTest {
        val error = useCase(hour = 12)(Money(50_00))

        assertNull(error)
        assertEquals(
            listOf(GoalContribution(1, LocalDate(2026, 10, 23), Money(50_00))),
            repository.observeContributions().first(),
        )
    }

    @Test
    fun `before 05h00 the contribution belongs to the previous app day`() = runTest {
        useCase(hour = 3)(Money(50_00))

        assertEquals(LocalDate(2026, 10, 22), repository.observeContributions().first().single().appDay)
    }

    @Test
    fun `a zero amount is rejected and stores nothing`() = runTest {
        val error = useCase(hour = 12)(Money.ZERO)

        assertEquals(ContributionValidationError.NonPositiveAmount, error)
        assertEquals(emptyList<GoalContribution>(), repository.observeContributions().first())
    }

    @Test
    fun `a missing amount is rejected`() = runTest {
        assertEquals(ContributionValidationError.NonPositiveAmount, useCase(hour = 12)(null))
    }

    @Test
    fun `a negative amount is rejected`() = runTest {
        assertEquals(ContributionValidationError.NonPositiveAmount, useCase(hour = 12)(Money(-5_00)))
    }
}
