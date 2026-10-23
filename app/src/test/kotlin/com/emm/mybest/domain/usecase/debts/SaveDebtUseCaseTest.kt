package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.validation.DebtDraft
import com.emm.mybest.domain.validation.DebtValidationError
import com.emm.mybest.testing.FakeDebtRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private val DRAFT = DebtDraft("  Tarjeta ", Money(320_000), Money(42_000), paymentDay = 15)

class SaveDebtUseCaseTest {

    private val repository = FakeDebtRepository()
    private val useCase = SaveDebtUseCase(repository)

    @Test
    fun `a valid draft is stored with a trimmed name`() = runTest {
        val errors = useCase(id = 0, draft = DRAFT)

        assertEquals(emptyList<DebtValidationError>(), errors)
        assertEquals(
            listOf(Debt(0, "Tarjeta", Money(320_000), Money(42_000), paymentDay = 15)),
            repository.observeDebts().first(),
        )
    }

    @Test
    fun `an invalid draft returns its errors and stores nothing`() = runTest {
        val errors = useCase(id = 0, draft = DRAFT.copy(name = "", paymentDay = 40))

        assertEquals(listOf(DebtValidationError.BlankName, DebtValidationError.PaymentDayOutOfRange), errors)
        assertEquals(emptyList<Debt>(), repository.observeDebts().first())
    }
}
