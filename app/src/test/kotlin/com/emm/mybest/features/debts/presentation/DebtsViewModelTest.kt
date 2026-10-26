package com.emm.mybest.features.debts.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.repository.DebtRepository
import com.emm.mybest.domain.usecase.debts.MarkInstallmentPaidUseCase
import com.emm.mybest.domain.usecase.debts.ObserveDebtListUseCase
import com.emm.mybest.domain.usecase.debts.SaveDebtUseCase
import com.emm.mybest.domain.validation.DebtValidationError
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
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
private val CARD = Debt(1, "Tarjeta", Money(320_000), Money(42_000), paymentDay = 15)
private val MOTO = Debt(2, "Moto", Money(215_000), Money(31_000), paymentDay = 5)
private val NOVEMBER = YearMonthValue(2026, 11)

private data class DebtFields(
    val name: String,
    val balance: String,
    val installment: String,
    val day: String,
)

class DebtsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = AppDayClock(FixedClock(LocalDateTime(2026, 10, 23, 12, 0).toInstant(LIMA)), LIMA)

    private fun repository(
        debts: List<Debt> = listOf(CARD, MOTO),
        payments: List<DebtPayment> = emptyList(),
    ) = FakeDebtRepository(debts, payments)

    private fun viewModel(repository: DebtRepository, actionRepository: DebtRepository = repository) = DebtsViewModel(
        DebtsUseCases(
            observeDebtList = ObserveDebtListUseCase(repository, clock),
            saveDebt = SaveDebtUseCase(actionRepository),
            markInstallmentPaid = MarkInstallmentPaidUseCase(actionRepository, clock),
            debtRepository = actionRepository,
        ),
    )

    private fun failingRepository(
        delegate: FakeDebtRepository,
        failure: Throwable = IllegalStateException("boom"),
    ): DebtRepository = object : DebtRepository by delegate {
        override suspend fun saveDebt(debt: Debt): Long = throw failure

        override suspend fun deleteDebt(id: Long) = throw failure

        override fun observeDebts() = flow<List<Debt>> { throw failure }
    }

    private fun gatedRepository(
        delegate: FakeDebtRepository,
        gate: CompletableDeferred<Unit>,
        attempts: MutableList<Debt>,
    ): DebtRepository = object : DebtRepository by delegate {
        override suspend fun saveDebt(debt: Debt): Long {
            attempts += debt
            gate.await()
            return delegate.saveDebt(debt)
        }
    }

    private fun failingOnceRepository(
        delegate: FakeDebtRepository,
        attempts: MutableList<Debt>,
    ): DebtRepository = object : DebtRepository by delegate {
        override suspend fun saveDebt(debt: Debt): Long {
            attempts += debt
            check(attempts.size > 1) { "boom" }
            return delegate.saveDebt(debt)
        }
    }

    private fun TestScope.stateOf(viewModel: DebtsViewModel): DebtsState {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        return viewModel.state.value
    }

    private fun TestScope.send(viewModel: DebtsViewModel, vararg intents: DebtsIntent): DebtsState {
        stateOf(viewModel)
        intents.forEach { viewModel.onIntent(it) }
        advanceUntilIdle()
        return viewModel.state.value
    }

    private fun fill(fields: DebtFields) = listOf(
        DebtsIntent.OnFormChange(DebtFormField.NAME, fields.name),
        DebtsIntent.OnFormChange(DebtFormField.BALANCE, fields.balance),
        DebtsIntent.OnFormChange(DebtFormField.INSTALLMENT, fields.installment),
        DebtsIntent.OnFormChange(DebtFormField.PAYMENT_DAY, fields.day),
    ).toTypedArray()

    @Test
    fun `lists every debt with formatted money and its paid flag`() = runTest {
        val payments = listOf(DebtPayment(MOTO.id, NOVEMBER, MOTO.installment))

        val state = stateOf(viewModel(repository(payments = payments)))

        assertTrue(state.isLoaded)
        assertEquals(
            listOf(
                DebtItemState(1, "Tarjeta", "S/ 3,200.00", "S/ 420.00", 15, isPaidThisMonth = false),
                DebtItemState(2, "Moto", "S/ 2,150.00", "S/ 310.00", 5, isPaidThisMonth = true),
            ),
            state.debts,
        )
    }

    @Test
    fun `without debts the list is empty once loaded`() = runTest {
        val state = stateOf(viewModel(repository(debts = emptyList())))

        assertTrue(state.isLoaded)
        assertTrue(state.debts.isEmpty())
    }

    @Test
    fun `adding a valid debt stores it and closes the form`() = runTest {
        val repository = repository(debts = emptyList())

        val state = send(
            viewModel(repository),
            DebtsIntent.OnAddClick,
            *fill(DebtFields("Préstamo", "1200.50", "150", "28")),
            DebtsIntent.OnFormSave,
        )

        assertNull(state.form)
        assertEquals(
            listOf(Debt(0, "Préstamo", Money(120_050), Money(15_000), paymentDay = 28)),
            repository.observeDebts().first(),
        )
    }

    @Test
    fun `saving an invalid form keeps it open with one error per field`() = runTest {
        val repository = repository(debts = emptyList())

        val state = send(
            viewModel(repository),
            DebtsIntent.OnAddClick,
            *fill(DebtFields("", "", "0", "32")),
            DebtsIntent.OnFormSave,
        )

        assertEquals(
            setOf(
                DebtValidationError.BlankName,
                DebtValidationError.MissingBalance,
                DebtValidationError.NonPositiveInstallment,
                DebtValidationError.PaymentDayOutOfRange,
            ),
            state.form?.errors,
        )
        assertTrue(repository.observeDebts().first().isEmpty())
    }

    @Test
    fun `a negative balance is reported as negative`() = runTest {
        val state = send(
            viewModel(repository(debts = emptyList())),
            DebtsIntent.OnAddClick,
            *fill(DebtFields("Moto", "-5", "10", "3")),
            DebtsIntent.OnFormSave,
        )

        assertEquals(setOf(DebtValidationError.NegativeBalance), state.form?.errors)
    }

    @Test
    fun `editing a field clears the errors`() = runTest {
        val viewModel = viewModel(repository(debts = emptyList()))
        send(viewModel, DebtsIntent.OnAddClick, DebtsIntent.OnFormSave)

        val state = send(viewModel, DebtsIntent.OnFormChange(DebtFormField.NAME, "Moto"))

        assertEquals("Moto", state.form?.name)
        assertTrue(state.form?.errors.orEmpty().isEmpty())
    }

    @Test
    fun `editing a debt opens the form with its values and updates it`() = runTest {
        val repository = repository()
        val viewModel = viewModel(repository)

        val opened = send(viewModel, DebtsIntent.OnEditClick(CARD.id))
        val saved = send(viewModel, DebtsIntent.OnFormChange(DebtFormField.NAME, "Visa"), DebtsIntent.OnFormSave)

        assertEquals(DebtFormState(1, "Tarjeta", "3200", "420", "15"), opened.form)
        assertNull(saved.form)
        assertEquals("Visa", repository.observeDebts().first().first { it.id == CARD.id }.name)
    }

    @Test
    fun `dismissing the form discards it`() = runTest {
        val state = send(viewModel(repository()), DebtsIntent.OnAddClick, DebtsIntent.OnFormDismiss)

        assertNull(state.form)
    }

    @Test
    fun `paying lowers the balance and flags the debt as paid`() = runTest {
        val state = send(viewModel(repository()), DebtsIntent.OnPayClick(CARD.id))

        val card = state.debts.first { it.id == CARD.id }
        assertTrue(card.isPaidThisMonth)
        assertEquals("S/ 2,780.00", card.balanceLabel)
        assertFalse(state.debts.first { it.id == MOTO.id }.isPaidThisMonth)
    }

    @Test
    fun `paying an unknown debt reports a failure`() = runTest {
        val viewModel = viewModel(repository())
        stateOf(viewModel)

        viewModel.effect.test {
            viewModel.onIntent(DebtsIntent.OnPayClick(99))
            assertEquals(DebtsEffect.ActionFailed, awaitItem())
        }
    }

    @Test
    fun `deleting asks for confirmation before removing the debt`() = runTest {
        val repository = repository()
        val viewModel = viewModel(repository)

        val asked = send(viewModel, DebtsIntent.OnDeleteClick(CARD.id))
        val confirmed = send(viewModel, DebtsIntent.OnDeleteConfirm)

        assertEquals(CARD.id, asked.pendingDelete?.id)
        assertEquals(2, asked.debts.size)
        assertNull(confirmed.pendingDelete)
        assertEquals(listOf(MOTO), repository.observeDebts().first())
    }

    @Test
    fun `declining the confirmation keeps the debt`() = runTest {
        val state = send(
            viewModel(repository()),
            DebtsIntent.OnDeleteClick(CARD.id),
            DebtsIntent.OnDeleteDismiss,
        )

        assertNull(state.pendingDelete)
        assertEquals(2, state.debts.size)
    }

    @Test
    fun `a failing save reports a failure and keeps the form open`() = runTest {
        val good = repository(debts = emptyList())
        val viewModel = viewModel(good, failingRepository(good))
        stateOf(viewModel)

        viewModel.effect.test {
            send(viewModel, DebtsIntent.OnAddClick, *fill(DebtFields("Moto", "100", "10", "3")), DebtsIntent.OnFormSave)
            assertEquals(DebtsEffect.ActionFailed, awaitItem())
        }
        assertEquals("Moto", viewModel.state.value.form?.name)
    }

    @Test
    fun `saving a debt that vanished reports a failure`() = runTest {
        val repository = repository()
        val viewModel = viewModel(repository)
        send(viewModel, DebtsIntent.OnEditClick(CARD.id))
        repository.deleteDebt(CARD.id)

        viewModel.effect.test {
            send(viewModel, DebtsIntent.OnFormSave)
            assertEquals(DebtsEffect.ActionFailed, awaitItem())
        }
    }

    @Test
    fun `a failing delete reports a failure and closes the dialog`() = runTest {
        val good = repository()
        val viewModel = viewModel(good, failingRepository(good))
        stateOf(viewModel)

        viewModel.effect.test {
            send(viewModel, DebtsIntent.OnDeleteClick(CARD.id), DebtsIntent.OnDeleteConfirm)
            assertEquals(DebtsEffect.ActionFailed, awaitItem())
        }
        assertNull(viewModel.state.value.pendingDelete)
    }

    @Test
    fun `editing an unknown debt reports a failure`() = runTest {
        val viewModel = viewModel(repository())
        stateOf(viewModel)

        viewModel.effect.test {
            send(viewModel, DebtsIntent.OnEditClick(99))
            assertEquals(DebtsEffect.ActionFailed, awaitItem())
        }
        assertNull(viewModel.state.value.form)
    }

    @Test
    fun `a failing debt lookup while editing reports a failure`() = runTest {
        val good = repository()
        val viewModel = viewModel(good, failingRepository(good))
        backgroundScope.launch { viewModel.state.collect {} }

        viewModel.effect.test {
            viewModel.onIntent(DebtsIntent.OnEditClick(CARD.id))
            assertEquals(DebtsEffect.ActionFailed, awaitItem())
        }
    }

    @Test
    fun `saving three times while the first save is in flight stores one debt`() = runTest {
        val good = repository(debts = emptyList())
        val gate = CompletableDeferred<Unit>()
        val attempts = mutableListOf<Debt>()
        val viewModel = viewModel(good, gatedRepository(good, gate, attempts))

        send(
            viewModel,
            DebtsIntent.OnAddClick,
            *fill(DebtFields("Dos", "100", "10", "20")),
            DebtsIntent.OnFormSave,
            DebtsIntent.OnFormSave,
            DebtsIntent.OnFormSave,
        )
        runCurrent()
        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, attempts.size)
        assertEquals(1, good.observeDebts().first().size)
        assertNull(viewModel.state.value.form)
    }

    @Test
    fun `a save rejected by validation does not block the next one`() = runTest {
        val repository = repository(debts = emptyList())
        val viewModel = viewModel(repository)

        send(viewModel, DebtsIntent.OnAddClick, *fill(DebtFields("", "100", "10", "20")), DebtsIntent.OnFormSave)
        val state = send(viewModel, DebtsIntent.OnFormChange(DebtFormField.NAME, "Dos"), DebtsIntent.OnFormSave)

        assertNull(state.form)
        assertEquals(listOf("Dos"), repository.observeDebts().first().map { it.name })
    }

    @Test
    fun `a failed save reports once and the next save is attempted again`() = runTest {
        val good = repository(debts = emptyList())
        val attempts = mutableListOf<Debt>()
        val viewModel = viewModel(good, failingOnceRepository(good, attempts))
        stateOf(viewModel)

        viewModel.effect.test {
            send(viewModel, DebtsIntent.OnAddClick, *fill(DebtFields("Dos", "100", "10", "20")), DebtsIntent.OnFormSave)
            assertEquals(DebtsEffect.ActionFailed, awaitItem())
            send(viewModel, DebtsIntent.OnFormSave)
            expectNoEvents()
        }
        assertEquals(2, attempts.size)
        assertEquals(1, good.observeDebts().first().size)
        assertNull(viewModel.state.value.form)
    }

    @Test
    fun `a cancelled save is not reported as a failure`() = runTest {
        val viewModel = viewModel(
            failingRepository(repository(debts = emptyList()), CancellationException("cancelled")),
        )
        stateOf(viewModel)

        viewModel.effect.test {
            send(viewModel, DebtsIntent.OnAddClick, *fill(DebtFields("Moto", "100", "10", "3")), DebtsIntent.OnFormSave)
            expectNoEvents()
        }
    }
}
