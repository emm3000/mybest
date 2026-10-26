package com.emm.mybest.features.checkin.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.LimitKind
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.domain.usecase.checkin.ObserveCheckInTaskUseCase
import com.emm.mybest.domain.usecase.checkin.RecordNoOutingUseCase
import com.emm.mybest.domain.usecase.checkin.RecordUnplannedOutingUseCase
import com.emm.mybest.domain.usecase.checkin.SaveCheckInUseCase
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import com.emm.mybest.ui.components.LimitOutcomeState
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val FRIDAY = LocalDate(2026, 10, 23)
private val THURSDAY = LocalDate(2026, 10, 22)
private val SATURDAY_MORNING = LocalDateTime(LocalDate(2026, 10, 24), LocalTime(9, 0))
private val LIMITS = Limits(Money(200_00), drinks = 4, returnTime = LocalTime(2, 0))
private val PLANNED = Outing(1, FRIDAY, OutingKind.PLANNED, LIMITS, checkIn = null)
private val OLDER_UNPLANNED = Outing(2, THURSDAY, OutingKind.UNPLANNED, limits = null, checkIn = null)
private val UNPLANNED = Outing(1, FRIDAY, OutingKind.UNPLANNED, limits = null, checkIn = null)

private class CountingOutingRepository(private val delegate: OutingRepository) : OutingRepository by delegate {

    var saveCheckInCalls = 0
        private set

    override suspend fun saveCheckIn(outingId: Long, checkIn: CheckIn) {
        saveCheckInCalls++
        delegate.saveCheckIn(outingId, checkIn)
    }
}

class CheckInViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: OutingRepository): CheckInViewModel {
        val clock = AppDayClock(FixedClock(SATURDAY_MORNING.toInstant(LIMA)), LIMA)
        return CheckInViewModel(
            CheckInUseCases(
                observeTask = ObserveCheckInTaskUseCase(repository, clock),
                saveCheckIn = SaveCheckInUseCase(repository),
                recordUnplannedOuting = RecordUnplannedOutingUseCase(repository),
                recordNoOuting = RecordNoOutingUseCase(repository),
            ),
        )
    }

    private suspend fun TestScope.stateOf(viewModel: CheckInViewModel): CheckInState {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        return viewModel.state.value
    }

    private fun TestScope.act(viewModel: CheckInViewModel, vararg intents: CheckInIntent): CheckInState {
        intents.forEach(viewModel::onIntent)
        advanceUntilIdle()
        return viewModel.state.value
    }

    private fun TestScope.observe(viewModel: CheckInViewModel): Job {
        val screen = backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        return screen
    }

    private suspend fun TestScope.leave(screen: Job) {
        screen.cancelAndJoin()
        advanceTimeBy(SUBSCRIPTION_TIMEOUT_MS + 1)
        runCurrent()
    }

    private fun TestScope.observeFromStart(viewModel: CheckInViewModel): List<CheckInState> {
        val seen = mutableListOf<CheckInState>()
        backgroundScope.launch { viewModel.state.collect(seen::add) }
        advanceUntilIdle()
        return seen
    }

    private val answerUnplanned = arrayOf(CheckInIntent.OnSpentChange("90"), CheckInIntent.OnMoodSelect(3))

    private val answerPlanned = arrayOf(
        CheckInIntent.OnSpentChange("260"),
        CheckInIntent.OnDrinksChange("5"),
        CheckInIntent.OnReturnTimeSelected(1, 40),
        CheckInIntent.OnMoodSelect(2),
    )

    @Test
    fun `a pending planned outing opens the form with its day and limits`() = runTest {
        val state = stateOf(viewModel(FakeOutingRepository(initialOutings = listOf(PLANNED))))

        assertTrue(state.isLoaded)
        assertEquals(CheckInStep.FORM, state.step)
        assertEquals("VIE · 23 OCT", state.dayLabel)
        assertEquals(CheckInLimitsState("S/ 200.00", "4", "02:00"), state.limits)
    }

    @Test
    fun `save is enabled only when all four planned inputs are set`() = runTest {
        val viewModel = viewModel(FakeOutingRepository(initialOutings = listOf(PLANNED)))
        stateOf(viewModel)

        val partial = act(viewModel, *answerPlanned.copyOfRange(0, 3))
        assertFalse(partial.canSave)

        val complete = act(viewModel, answerPlanned.last())
        assertTrue(complete.canSave)
        assertEquals("01:40", complete.returnTimeLabel)
    }

    @Test
    fun `an unplanned outing needs only the spend and the mood`() = runTest {
        val viewModel = viewModel(FakeOutingRepository(initialOutings = listOf(UNPLANNED)))
        stateOf(viewModel)

        val state = act(viewModel, CheckInIntent.OnSpentChange("90"), CheckInIntent.OnMoodSelect(3))

        assertNull(state.limits)
        assertTrue(state.canSave)
    }

    @Test
    fun `saving shows how far each limit was exceeded`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(PLANNED))
        val viewModel = viewModel(repository)
        stateOf(viewModel)

        val state = act(viewModel, *answerPlanned, CheckInIntent.OnSaveClick)

        assertEquals(CheckInStep.RESULT, state.step)
        assertEquals(
            listOf(
                LimitOutcomeState.MoneyExceeded("S/ 60.00"),
                LimitOutcomeState.DrinksExceeded(1),
                LimitOutcomeState.Kept(LimitKind.RETURN_TIME),
            ),
            state.outcomes,
        )
        assertEquals(
            CheckIn(Money(260_00), 5, LocalTime(1, 40), mood = 2),
            repository.observeAll().first().single().checkIn,
        )
    }

    @Test
    fun `save does nothing while an input is missing`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(PLANNED))
        val viewModel = viewModel(repository)
        stateOf(viewModel)

        val state = act(viewModel, CheckInIntent.OnSpentChange("260"), CheckInIntent.OnSaveClick)

        assertEquals(CheckInStep.FORM, state.step)
        assertTrue(repository.observeAll().first().single().isPendingCheckIn)
    }

    @Test
    fun `a return in the morning after counts as exceeding the return time`() = runTest {
        val viewModel = viewModel(FakeOutingRepository(initialOutings = listOf(PLANNED)))
        stateOf(viewModel)

        val state = act(
            viewModel,
            CheckInIntent.OnSpentChange("150"),
            CheckInIntent.OnDrinksChange("4"),
            CheckInIntent.OnReturnTimeSelected(8, 30),
            CheckInIntent.OnMoodSelect(2),
            CheckInIntent.OnSaveClick,
        )

        assertEquals(
            listOf(
                LimitOutcomeState.Kept(LimitKind.MONEY),
                LimitOutcomeState.Kept(LimitKind.DRINKS),
                LimitOutcomeState.ReturnTimeExceeded(390),
            ),
            state.outcomes,
        )
    }

    @Test
    fun `done after saving closes the screen`() = runTest {
        val viewModel = viewModel(FakeOutingRepository(initialOutings = listOf(PLANNED)))
        stateOf(viewModel)
        act(viewModel, *answerPlanned, CheckInIntent.OnSaveClick)

        viewModel.effect.test {
            viewModel.onIntent(CheckInIntent.OnDoneClick)
            assertEquals(CheckInEffect.Close, awaitItem())
        }
    }

    @Test
    fun `a new pending outing after done opens its form, not the old result`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(PLANNED))
        val viewModel = viewModel(repository)
        val screen = observe(viewModel)
        act(viewModel, *answerPlanned, CheckInIntent.OnSaveClick)
        act(viewModel, CheckInIntent.OnDoneClick)
        leave(screen)

        repository.recordUnplanned(THURSDAY)
        val seen = observeFromStart(viewModel)

        assertTrue(seen.none { it.step == CheckInStep.RESULT })
        assertEquals(CheckInStep.FORM, seen.last().step)
        assertEquals("JUE · 22 OCT", seen.last().dayLabel)
    }

    @Test
    fun `done keeps the result on screen while the screen closes`() = runTest {
        val viewModel = viewModel(FakeOutingRepository(initialOutings = listOf(PLANNED)))
        stateOf(viewModel)
        val result = act(viewModel, *answerPlanned, CheckInIntent.OnSaveClick)

        viewModel.effect.test {
            val closing = act(viewModel, CheckInIntent.OnDoneClick)
            assertEquals(CheckInEffect.Close, awaitItem())
            assertEquals(result, closing)
        }
    }

    @Test
    fun `done with another outing pending closes and keeps the result on screen`() = runTest {
        val viewModel = viewModel(FakeOutingRepository(initialOutings = listOf(OLDER_UNPLANNED, PLANNED)))
        stateOf(viewModel)
        val result = act(viewModel, *answerUnplanned, CheckInIntent.OnSaveClick)

        viewModel.effect.test {
            val closing = act(viewModel, CheckInIntent.OnDoneClick)
            assertEquals(CheckInEffect.Close, awaitItem())
            assertEquals(result, closing)
        }
        assertEquals(CheckInStep.RESULT, result.step)
        assertEquals("JUE · 22 OCT", result.dayLabel)
    }

    @Test
    fun `the next pending outing opens its form once the screen was left`() = runTest {
        val viewModel = viewModel(FakeOutingRepository(initialOutings = listOf(OLDER_UNPLANNED, PLANNED)))
        val screen = observe(viewModel)
        act(viewModel, *answerUnplanned, CheckInIntent.OnSaveClick)
        act(viewModel, CheckInIntent.OnDoneClick)
        leave(screen)

        val seen = observeFromStart(viewModel)

        assertTrue(seen.none { it.step == CheckInStep.RESULT })
        assertEquals("VIE · 23 OCT", seen.last().dayLabel)
    }

    @Test
    fun `saving twice in a row saves once and shows no error`() = runTest {
        val repository = CountingOutingRepository(FakeOutingRepository(initialOutings = listOf(PLANNED)))
        val viewModel = viewModel(repository)
        stateOf(viewModel)
        act(viewModel, *answerPlanned)

        viewModel.effect.test {
            viewModel.onIntent(CheckInIntent.OnSaveClick)
            viewModel.onIntent(CheckInIntent.OnSaveClick)
            advanceUntilIdle()
            expectNoEvents()
        }

        assertEquals(1, repository.saveCheckInCalls)
    }

    @Test
    fun `not now closes the screen and leaves the outing pending`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(PLANNED))
        val viewModel = viewModel(repository)
        stateOf(viewModel)

        viewModel.effect.test {
            viewModel.onIntent(CheckInIntent.OnNotNowClick)
            assertEquals(CheckInEffect.Close, awaitItem())
        }

        assertEquals(listOf(PLANNED), repository.observeAll().first())
    }

    @Test
    fun `a weekend morning with no outing asks if the user went out`() = runTest {
        val state = stateOf(viewModel(FakeOutingRepository()))

        assertEquals(CheckInStep.QUESTION, state.step)
        assertEquals("VIE · 23 OCT", state.dayLabel)
    }

    @Test
    fun `yes replaces the won night with an unplanned outing and opens its form`() = runTest {
        val repository = FakeOutingRepository(initialWonNights = listOf(WonNight(FRIDAY, Money(380_00))))
        val viewModel = viewModel(repository)
        stateOf(viewModel)

        val state = act(viewModel, CheckInIntent.OnWentOutYesClick)

        assertEquals(CheckInStep.FORM, state.step)
        assertEquals(OutingKind.UNPLANNED, repository.observeAll().first().single().kind)
        assertTrue(repository.observeWonNights(FRIDAY, FRIDAY).first().isEmpty())
    }

    @Test
    fun `no closes the screen and the question does not come back`() = runTest {
        val repository = FakeOutingRepository()
        val viewModel = viewModel(repository)
        stateOf(viewModel)

        viewModel.effect.test {
            viewModel.onIntent(CheckInIntent.OnWentOutNoClick)
            assertEquals(CheckInEffect.Close, awaitItem())
        }

        advanceUntilIdle()
        assertEquals(CheckInStep.NO_TASK, viewModel.state.value.step)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `tapping yes twice records one outing and shows no error`() = runTest {
        val repository = FakeOutingRepository()
        val viewModel = viewModel(repository)
        stateOf(viewModel)

        viewModel.effect.test {
            viewModel.onIntent(CheckInIntent.OnWentOutYesClick)
            viewModel.onIntent(CheckInIntent.OnWentOutYesClick)
            advanceUntilIdle()
            expectNoEvents()
        }

        assertEquals(1, repository.observeAll().first().size)
    }

    @Test
    fun `tapping no twice closes the screen once`() = runTest {
        val viewModel = viewModel(FakeOutingRepository())
        stateOf(viewModel)

        viewModel.effect.test {
            viewModel.onIntent(CheckInIntent.OnWentOutNoClick)
            viewModel.onIntent(CheckInIntent.OnWentOutNoClick)
            advanceUntilIdle()
            assertEquals(CheckInEffect.Close, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `saving works after the answer to the question finished`() = runTest {
        val viewModel = viewModel(FakeOutingRepository())
        stateOf(viewModel)
        act(viewModel, CheckInIntent.OnWentOutYesClick)

        val state = act(viewModel, CheckInIntent.OnSpentChange("90"), CheckInIntent.OnMoodSelect(3))
        val saved = act(viewModel, CheckInIntent.OnSaveClick)

        assertTrue(state.canSave)
        assertEquals(CheckInStep.RESULT, saved.step)
    }
}
