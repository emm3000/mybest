package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.models.InstallmentShare
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.PreOutingSnapshot
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.repository.DebtRepository
import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")

private class CountingDebtRepository(
    private val delegate: FakeDebtRepository,
) : DebtRepository by delegate {
    var debtCollects = 0
    var paymentCollects = 0

    override fun observeDebts(): Flow<List<Debt>> = delegate.observeDebts().onStart { debtCollects++ }

    override fun observePayments(): Flow<List<DebtPayment>> = delegate.observePayments().onStart { paymentCollects++ }
}

private class CountingOutingRepository(
    private val delegate: FakeOutingRepository,
) : OutingRepository by delegate {
    var allCollects = 0

    override fun observeAll(): Flow<List<Outing>> = delegate.observeAll().onStart { allCollects++ }
}

class ObservePreOutingSnapshotSubscriptionTest {

    private val clock = AppDayClock(
        FixedClock(LocalDateTime(LocalDate(2026, 10, 23), LocalTime(12, 0)).toInstant(LIMA)),
        LIMA,
    )
    private val debtRepository = CountingDebtRepository(
        FakeDebtRepository(
            listOf(Debt(1, "Banco", balance = Money(5_000_00), installment = Money(400_00), paymentDay = 28)),
        ),
    )
    private val outingRepository = CountingOutingRepository(FakeOutingRepository())
    private val setupRepository = FakeSetupRepository(Setup(typicalOutingCost = Money(380_00)))
    private val estimate = GetEstimatedOutingCostUseCase(setupRepository, outingRepository)
    private val useCase = ObservePreOutingSnapshotUseCase(
        PreOutingUseCases(
            outingMonth = ObserveOutingMonthUseCase(outingRepository, setupRepository, clock),
            installmentInputs = ObserveInstallmentInputsUseCase(debtRepository, clock),
            purchaseGoalLeft = ObservePurchaseGoalLeftUseCase(FakePurchaseGoalRepository(null, emptyList())),
            estimatedOutingCost = estimate,
            notSpentThisMonth = ObserveNotSpentThisMonthUseCase(outingRepository, clock),
        ),
    )

    @Test
    fun `one snapshot reads the debts once`() = runTest {
        useCase().first()

        assertEquals(1, debtRepository.debtCollects)
    }

    @Test
    fun `one snapshot reads the payments once`() = runTest {
        useCase().first()

        assertEquals(1, debtRepository.paymentCollects)
    }

    @Test
    fun `one snapshot reads every outing once`() = runTest {
        useCase().first()

        assertEquals(1, outingRepository.allCollects)
    }

    @Test
    fun `the snapshot keeps its values`() = runTest {
        val expected = PreOutingSnapshot(
            spentOnOutingsThisMonth = Money.ZERO,
            outingsThisMonth = 0,
            monthlyOutingLimit = null,
            isNextOutingOverLimit = false,
            estimatedOutingCost = Money(380_00),
            isEstimateAveraged = false,
            hasDebts = true,
            nearestInstallment = InstallmentShare("Banco", LocalDate(2026, 10, 28), Money(400_00), nightPercent = 95),
            purchaseGoalLeft = null,
            notSpentThisMonth = Money.ZERO,
        )

        assertEquals(expected, useCase().first())
    }
}
