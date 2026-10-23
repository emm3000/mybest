package com.emm.mybest.domain.usecase.goal

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.validation.PurchaseGoalDraft
import com.emm.mybest.domain.validation.PurchaseGoalValidationError
import com.emm.mybest.testing.FakePurchaseGoalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val DRAFT = PurchaseGoalDraft("  Zapatillas ", Money(450_00))

class SavePurchaseGoalUseCaseTest {

    private val repository = FakePurchaseGoalRepository()
    private val useCase = SavePurchaseGoalUseCase(repository)

    @Test
    fun `a valid draft is stored with a trimmed name`() = runTest {
        val errors = useCase(DRAFT)

        assertEquals(emptyList<PurchaseGoalValidationError>(), errors)
        assertEquals(PurchaseGoal("Zapatillas", Money(450_00)), repository.observeGoal().first())
    }

    @Test
    fun `a blank name is rejected and stores nothing`() = runTest {
        val errors = useCase(DRAFT.copy(name = "   "))

        assertEquals(listOf(PurchaseGoalValidationError.BlankName), errors)
        assertNull(repository.observeGoal().first())
    }

    @Test
    fun `a zero price is rejected`() = runTest {
        assertEquals(listOf(PurchaseGoalValidationError.NonPositivePrice), useCase(DRAFT.copy(price = Money.ZERO)))
    }

    @Test
    fun `a missing price is rejected`() = runTest {
        assertEquals(listOf(PurchaseGoalValidationError.NonPositivePrice), useCase(DRAFT.copy(price = null)))
    }

    @Test
    fun `a blank name and a bad price report both errors`() = runTest {
        val errors = useCase(PurchaseGoalDraft("", Money.ZERO))

        assertEquals(
            listOf(PurchaseGoalValidationError.BlankName, PurchaseGoalValidationError.NonPositivePrice),
            errors,
        )
        assertNull(repository.observeGoal().first())
    }
}
