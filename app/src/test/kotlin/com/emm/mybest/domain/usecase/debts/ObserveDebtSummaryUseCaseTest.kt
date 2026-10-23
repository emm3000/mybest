package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtSummary
import com.emm.mybest.testing.FakeDebtRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveDebtSummaryUseCaseTest {

    @Test
    fun `counts the debts and sums their balances`() = runTest {
        val debts = listOf(
            Debt(1, "Tarjeta", Money(320_000), Money(42_000), paymentDay = 15),
            Debt(2, "Moto", Money(215_000), Money(31_000), paymentDay = 5),
        )

        val summary = ObserveDebtSummaryUseCase(FakeDebtRepository(debts))().first()

        assertEquals(DebtSummary(count = 2, totalBalance = Money(535_000)), summary)
    }

    @Test
    fun `is empty without debts`() = runTest {
        assertEquals(DebtSummary(0, Money.ZERO), ObserveDebtSummaryUseCase(FakeDebtRepository())().first())
    }
}
