package com.emm.mybest.features.goal.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.usecase.goal.AddGoalContributionUseCase
import com.emm.mybest.domain.usecase.goal.ObserveGoalProgressUseCase
import com.emm.mybest.domain.usecase.goal.SavePurchaseGoalUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveNotSpentThisMonthUseCase
import com.emm.mybest.domain.validation.ContributionValidationError
import com.emm.mybest.domain.validation.PurchaseGoalValidationError
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val GOAL = PurchaseGoal("Zapatillas", Money(450_00))
private val OCTOBER_9 = LocalDate(2026, 10, 9)
private val OCTOBER_16 = LocalDate(2026, 10, 16)
private val OCTOBER_23 = LocalDate(2026, 10, 23)

class PurchaseGoalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = AppDayClock(FixedClock(LocalDateTime(2026, 10, 23, 12, 0).toInstant(LIMA)), LIMA)

    private fun viewModel(
        repository: PurchaseGoalRepository,
        wonNights: List<WonNight> = emptyList(),
    ) = PurchaseGoalViewModel(
        PurchaseGoalUseCases(
            observeGoalProgress = ObserveGoalProgressUseCase(repository),
            observeNotSpent = ObserveNotSpentThisMonthUseCase(
                FakeOutingRepository(initialWonNights = wonNights),
                clock,
            ),
            saveGoal = SavePurchaseGoalUseCase(repository),
            addContribution = AddGoalContributionUseCase(repository, clock),
            purchaseGoalRepository = repository,
        ),
    )

    private fun failingRepository(delegate: FakePurchaseGoalRepository): PurchaseGoalRepository =
        object : PurchaseGoalRepository by delegate {
            override suspend fun addContribution(appDay: LocalDate, amount: Money) = error("boom")

            override suspend fun deleteContribution(id: Long) = error("boom")

            override suspend fun clearGoal() = error("boom")
        }

    private fun gatedRepository(
        delegate: FakePurchaseGoalRepository,
        gate: CompletableDeferred<Unit>,
        attempts: MutableList<Money>,
    ): PurchaseGoalRepository = object : PurchaseGoalRepository by delegate {
        override suspend fun addContribution(appDay: LocalDate, amount: Money) {
            attempts += amount
            gate.await()
            delegate.addContribution(appDay, amount)
        }
    }

    private fun failingOnceRepository(
        delegate: FakePurchaseGoalRepository,
        attempts: MutableList<Money>,
    ): PurchaseGoalRepository = object : PurchaseGoalRepository by delegate {
        override suspend fun addContribution(appDay: LocalDate, amount: Money) {
            attempts += amount
            check(attempts.size > 1) { "boom" }
            delegate.addContribution(appDay, amount)
        }
    }

    private fun TestScope.send(viewModel: PurchaseGoalViewModel, vararg intents: PurchaseGoalIntent): PurchaseGoalState {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        intents.forEach { viewModel.onIntent(it) }
        advanceUntilIdle()
        return viewModel.state.value
    }

    private fun defineGoal(name: String, price: String) = arrayOf(
        PurchaseGoalIntent.OnGoalChange(GoalFormField.NAME, name),
        PurchaseGoalIntent.OnGoalChange(GoalFormField.PRICE, price),
        PurchaseGoalIntent.OnGoalSave,
    )

    private fun contribute(amount: String) = arrayOf(
        PurchaseGoalIntent.OnAddContributionClick,
        PurchaseGoalIntent.OnContributionChange(amount),
        PurchaseGoalIntent.OnContributionSave,
    )

    @Test
    fun `shows no goal until one is defined`() = runTest {
        val state = send(viewModel(FakePurchaseGoalRepository()))

        assertTrue(state.isLoaded)
        assertNull(state.goal)
        assertEquals(GoalFormState(), state.goalForm)
    }

    @Test
    fun `defining a goal shows its price and zero progress`() = runTest {
        val repository = FakePurchaseGoalRepository()

        val state = send(viewModel(repository), *defineGoal("Zapatillas", "450"))

        val goal = checkNotNull(state.goal)
        assertEquals("Zapatillas", goal.name)
        assertEquals("S/ 450.00", goal.priceLabel)
        assertEquals("S/ 0.00", goal.contributedLabel)
        assertEquals("S/ 450.00", goal.leftLabel)
        assertEquals(0, goal.percent)
        assertEquals(GoalFormState(), state.goalForm)
    }

    @Test
    fun `a blank name keeps the form with its error and stores nothing`() = runTest {
        val repository = FakePurchaseGoalRepository()

        val state = send(viewModel(repository), *defineGoal("  ", "450"))

        assertNull(state.goal)
        assertEquals(setOf(PurchaseGoalValidationError.BlankName), state.goalForm.errors)
        assertNull(repository.observeGoal().first())
    }

    @Test
    fun `a non-positive price is rejected`() = runTest {
        val zero = send(viewModel(FakePurchaseGoalRepository()), *defineGoal("Zapatillas", "0"))
        val text = send(viewModel(FakePurchaseGoalRepository()), *defineGoal("Zapatillas", "abc"))

        assertEquals(setOf(PurchaseGoalValidationError.NonPositivePrice), zero.goalForm.errors)
        assertEquals(setOf(PurchaseGoalValidationError.NonPositivePrice), text.goalForm.errors)
    }

    @Test
    fun `editing a field clears the form errors`() = runTest {
        val viewModel = viewModel(FakePurchaseGoalRepository())
        send(viewModel, *defineGoal("", "450"))

        val state = send(viewModel, PurchaseGoalIntent.OnGoalChange(GoalFormField.NAME, "Zap"))

        assertEquals(emptySet<PurchaseGoalValidationError>(), state.goalForm.errors)
    }

    @Test
    fun `a contribution moves the progress and is listed`() = runTest {
        val repository = FakePurchaseGoalRepository(GOAL)

        val state = send(viewModel(repository), *contribute("180"))

        val goal = checkNotNull(state.goal)
        assertEquals("S/ 180.00", goal.contributedLabel)
        assertEquals("S/ 270.00", goal.leftLabel)
        assertEquals(40, goal.percent)
        assertEquals(0.4f, goal.progressFraction, 0.0001f)
        assertEquals("ZAPATILLAS", goal.nameLabel)
        assertEquals(listOf("S/ 180.00"), goal.contributions.map { it.amountLabel })
        assertNull(state.contributionForm)
    }

    @Test
    fun `contributions are listed newest first`() = runTest {
        val contributions = listOf(
            GoalContribution(1, OCTOBER_9, Money(10_00)),
            GoalContribution(2, OCTOBER_23, Money(30_00)),
            GoalContribution(3, OCTOBER_16, Money(20_00)),
        )

        val state = send(viewModel(FakePurchaseGoalRepository(GOAL, contributions)))

        assertEquals(listOf(2L, 3L, 1L), checkNotNull(state.goal).contributions.map { it.id })
        assertEquals("Viernes 23 octubre", checkNotNull(state.goal).contributions.first().dayLabel)
    }

    @Test
    fun `a non-positive contribution keeps the form with its error`() = runTest {
        val repository = FakePurchaseGoalRepository(GOAL)

        val state = send(viewModel(repository), *contribute("0"))

        assertEquals(ContributionValidationError.NonPositiveAmount, state.contributionForm?.error)
        checkNotNull(state.goal)
        val contributed = repository.observeContributions().first().fold(Money.ZERO) { total, c -> total + c.amount }
        assertEquals(Money.ZERO, contributed)
    }

    @Test
    fun `dismissing the contribution form closes it`() = runTest {
        val state = send(
            viewModel(FakePurchaseGoalRepository(GOAL)),
            PurchaseGoalIntent.OnAddContributionClick,
            PurchaseGoalIntent.OnContributionDismiss,
        )

        assertNull(state.contributionForm)
    }

    @Test
    fun `deleting a contribution reduces the progress`() = runTest {
        val contributions =
            listOf(GoalContribution(1, OCTOBER_9, Money(100_00)), GoalContribution(2, OCTOBER_16, Money(80_00)))

        val state = send(
            viewModel(FakePurchaseGoalRepository(GOAL, contributions)),
            PurchaseGoalIntent.OnContributionDelete(2),
        )

        assertEquals("S/ 100.00", checkNotNull(state.goal).contributedLabel)
        assertEquals(listOf(1L), checkNotNull(state.goal).contributions.map { it.id })
    }

    @Test
    fun `won nights fill the estimate card and never the progress`() = runTest {
        val wonNights = listOf(WonNight(OCTOBER_9, Money(380_00)))

        val state = send(viewModel(FakePurchaseGoalRepository(GOAL), wonNights))

        assertEquals("S/ 380.00", state.notSpentLabel)
        assertEquals("S/ 0.00", checkNotNull(state.goal).contributedLabel)
        assertEquals(0, checkNotNull(state.goal).percent)
    }

    @Test
    fun `clearing asks for confirmation before removing the goal and its contributions`() = runTest {
        val repository = FakePurchaseGoalRepository(GOAL, listOf(GoalContribution(1, OCTOBER_9, Money(100_00))))
        val viewModel = viewModel(repository)

        val asking = send(viewModel, PurchaseGoalIntent.OnClearClick)

        assertTrue(asking.isClearConfirmationVisible)
        assertEquals(GOAL, repository.observeGoal().first())

        val dismissed = send(viewModel, PurchaseGoalIntent.OnClearDismiss)

        assertFalse(dismissed.isClearConfirmationVisible)
        assertEquals(GOAL, repository.observeGoal().first())

        val cleared = send(viewModel, PurchaseGoalIntent.OnClearClick, PurchaseGoalIntent.OnClearConfirm)

        assertNull(cleared.goal)
        assertFalse(cleared.isClearConfirmationVisible)
        assertEquals(emptyList<GoalContribution>(), repository.observeContributions().first())
    }

    @Test
    fun `a failing action emits an effect`() = runTest {
        val viewModel = viewModel(failingRepository(FakePurchaseGoalRepository(GOAL)))

        viewModel.effect.test {
            send(viewModel, *contribute("50"))
            assertEquals(PurchaseGoalEffect.ActionFailed, awaitItem())
        }
    }

    @Test
    fun `saving three times while the first save is in flight stores one contribution`() = runTest {
        val good = FakePurchaseGoalRepository(GOAL)
        val gate = CompletableDeferred<Unit>()
        val attempts = mutableListOf<Money>()
        val viewModel = viewModel(gatedRepository(good, gate, attempts))

        send(
            viewModel,
            PurchaseGoalIntent.OnAddContributionClick,
            PurchaseGoalIntent.OnContributionChange("20"),
            PurchaseGoalIntent.OnContributionSave,
            PurchaseGoalIntent.OnContributionSave,
            PurchaseGoalIntent.OnContributionSave,
        )
        runCurrent()
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(Money(20_00)), attempts)
        assertEquals(listOf(Money(20_00)), good.observeContributions().first().map { it.amount })
        assertNull(viewModel.state.value.contributionForm)
    }

    @Test
    fun `a contribution rejected by validation does not block the next one`() = runTest {
        val repository = FakePurchaseGoalRepository(GOAL)
        val viewModel = viewModel(repository)

        send(viewModel, *contribute("0"))
        val state = send(
            viewModel,
            PurchaseGoalIntent.OnContributionChange("20"),
            PurchaseGoalIntent.OnContributionSave,
        )

        assertNull(state.contributionForm)
        assertEquals(listOf(Money(20_00)), repository.observeContributions().first().map { it.amount })
    }

    @Test
    fun `a failed contribution reports once and the next save is attempted again`() = runTest {
        val good = FakePurchaseGoalRepository(GOAL)
        val attempts = mutableListOf<Money>()
        val viewModel = viewModel(failingOnceRepository(good, attempts))

        viewModel.effect.test {
            send(viewModel, *contribute("20"))
            assertEquals(PurchaseGoalEffect.ActionFailed, awaitItem())
            send(viewModel, PurchaseGoalIntent.OnContributionSave)
            expectNoEvents()
        }
        assertEquals(2, attempts.size)
        assertEquals(listOf(Money(20_00)), good.observeContributions().first().map { it.amount })
        assertNull(viewModel.state.value.contributionForm)
    }
}
