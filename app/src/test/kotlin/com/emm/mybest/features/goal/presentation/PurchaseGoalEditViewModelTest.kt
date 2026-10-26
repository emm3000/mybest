package com.emm.mybest.features.goal.presentation

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.validation.PurchaseGoalValidationError
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val GOAL = PurchaseGoal("Zapatillas", Money(450_00))
private val OCTOBER_9 = LocalDate(2026, 10, 9)

class PurchaseGoalEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakePurchaseGoalRepository) = purchaseGoalViewModel(repository)

    @Test
    fun `editing a goal prefills the form with its name and price`() = runTest {
        val state = send(viewModel(FakePurchaseGoalRepository(GOAL)), PurchaseGoalIntent.OnEditClick)

        assertTrue(state.goalForm.isEditing)
        assertEquals("Zapatillas", state.goalForm.name)
        assertEquals("450", state.goalForm.price)
    }

    @Test
    fun `editing a goal keeps the cents in the prefilled price`() = runTest {
        val repository = FakePurchaseGoalRepository(PurchaseGoal("Zapatillas", Money(450_50)))

        val state = send(viewModel(repository), PurchaseGoalIntent.OnEditClick)

        assertEquals("450.50", state.goalForm.price)
    }

    @Test
    fun `saving an edited price keeps the contributions and recomputes the progress`() = runTest {
        val repository = FakePurchaseGoalRepository(GOAL, listOf(GoalContribution(1, OCTOBER_9, Money(100_00))))

        val state = send(
            viewModel(repository),
            PurchaseGoalIntent.OnEditClick,
            PurchaseGoalIntent.OnGoalChange(GoalFormField.PRICE, "500"),
            PurchaseGoalIntent.OnGoalSave,
        )

        val goal = checkNotNull(state.goal)
        assertEquals(PurchaseGoal("Zapatillas", Money(500_00)), repository.observeGoal().first())
        assertFalse(state.goalForm.isEditing)
        assertEquals("S/ 500.00", goal.priceLabel)
        assertEquals("S/ 100.00", goal.contributedLabel)
        assertEquals("S/ 400.00", goal.leftLabel)
        assertEquals(20, goal.percent)
        assertEquals(listOf("S/ 100.00"), goal.contributions.map { it.amountLabel })
    }

    @Test
    fun `saving an edited name keeps the contributions`() = runTest {
        val repository = FakePurchaseGoalRepository(GOAL, listOf(GoalContribution(1, OCTOBER_9, Money(100_00))))

        val state = send(
            viewModel(repository),
            PurchaseGoalIntent.OnEditClick,
            PurchaseGoalIntent.OnGoalChange(GoalFormField.NAME, "Bicicleta"),
            PurchaseGoalIntent.OnGoalSave,
        )

        assertEquals(PurchaseGoal("Bicicleta", Money(450_00)), repository.observeGoal().first())
        assertEquals(listOf("S/ 100.00"), checkNotNull(state.goal).contributions.map { it.amountLabel })
    }

    @Test
    fun `saving a blank edited name keeps the edit form with its error and the stored goal`() = runTest {
        val repository = FakePurchaseGoalRepository(GOAL)

        val state = send(
            viewModel(repository),
            PurchaseGoalIntent.OnEditClick,
            PurchaseGoalIntent.OnGoalChange(GoalFormField.NAME, " "),
            PurchaseGoalIntent.OnGoalSave,
        )

        assertTrue(state.goalForm.isEditing)
        assertEquals(setOf(PurchaseGoalValidationError.BlankName), state.goalForm.errors)
        assertEquals(GOAL, repository.observeGoal().first())
    }

    @Test
    fun `cancelling an edit stores nothing and the next edit prefills the stored values`() = runTest {
        val repository = FakePurchaseGoalRepository(GOAL)
        val viewModel = viewModel(repository)

        val cancelled = send(
            viewModel,
            PurchaseGoalIntent.OnEditClick,
            PurchaseGoalIntent.OnGoalChange(GoalFormField.PRICE, "999"),
            PurchaseGoalIntent.OnEditCancel,
        )

        assertEquals(GoalFormState(), cancelled.goalForm)
        assertEquals(GOAL, repository.observeGoal().first())

        val reopened = send(viewModel, PurchaseGoalIntent.OnEditClick)

        assertEquals("450", reopened.goalForm.price)
    }

    @Test
    fun `clearing the goal while editing leaves the define form empty`() = runTest {
        val state = send(
            viewModel(FakePurchaseGoalRepository(GOAL)),
            PurchaseGoalIntent.OnEditClick,
            PurchaseGoalIntent.OnClearClick,
            PurchaseGoalIntent.OnClearConfirm,
        )

        assertNull(state.goal)
        assertEquals(GoalFormState(), state.goalForm)
    }
}
