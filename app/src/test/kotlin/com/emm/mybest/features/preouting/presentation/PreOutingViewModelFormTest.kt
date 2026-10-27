package com.emm.mybest.features.preouting.presentation

import com.emm.mybest.domain.models.PreOutingSnapshot
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PreOutingViewModelFormTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val outingRepository = FakeOutingRepository()
    private val gatedOutingRepository = GatedOutingRepository(outingRepository)

    private fun viewModel(
        snapshot: PreOutingSnapshot,
        goal: PurchaseGoal? = null,
        scenario: Scenario = Scenario(),
    ): PreOutingViewModel =
        preOutingViewModel(snapshot, goal, decisionUseCases(scenario, outingRepository, gatedOutingRepository))

    @Test
    fun `going out opens the form and dismissing closes it`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)

        viewModel.onIntent(PreOutingIntent.OnGoOutClick)
        assertTrue(stateNow(viewModel).form.isOpen)
        viewModel.onIntent(PreOutingIntent.OnFormDismiss)

        assertFalse(stateNow(viewModel).form.isOpen)
    }

    @Test
    fun `confirming needs the three limits and both checklist items`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        viewModel.onIntent(PreOutingIntent.OnGoOutClick)
        viewModel.onIntent(PreOutingIntent.OnMoneyChange("150"))
        viewModel.onIntent(PreOutingIntent.OnDrinksChange("4"))
        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(1, 30))
        viewModel.onIntent(PreOutingIntent.OnCardsOffToggle)
        assertFalse(stateNow(viewModel).canConfirmOuting)

        viewModel.onIntent(PreOutingIntent.OnAccountLoadedToggle)

        assertTrue(stateNow(viewModel).canConfirmOuting)
        assertEquals("S/ 150.00", stateNow(viewModel).moneyLimitLabel)
        assertEquals("01:30", stateNow(viewModel).returnTimeLabel)
    }

    @Test
    fun `a daytime return limit is flagged and cannot be confirmed`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        fillForm(viewModel)

        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(15, 0))
        val state = stateNow(viewModel)
        confirm(viewModel)

        assertEquals(0, gatedOutingRepository.recordPlannedCalls)
        assertTrue(state.isReturnTimeOutOfRange)
        assertFalse(state.canConfirmOuting)
        assertEquals("15:00", state.returnTimeLabel)
        assertTrue(outingRepository.observeAll().first().isEmpty())
        assertEquals(PreOutingDecisionState.Undecided, stateNow(viewModel).decision)
    }

    @Test
    fun `picking an allowed time after a daytime one clears the flag`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        fillForm(viewModel)
        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(15, 0))
        assertTrue(stateNow(viewModel).isReturnTimeOutOfRange)

        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(1, 30))

        assertFalse(stateNow(viewModel).isReturnTimeOutOfRange)
        assertTrue(stateNow(viewModel).canConfirmOuting)
    }

    @Test
    fun `the 18_00 and 04_59 return limits are not flagged`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        fillForm(viewModel)

        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(18, 0))
        assertFalse(stateNow(viewModel).isReturnTimeOutOfRange)
        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(4, 59))

        assertFalse(stateNow(viewModel).isReturnTimeOutOfRange)
    }

    @Test
    fun `no return time chosen is not flagged`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        viewModel.onIntent(PreOutingIntent.OnGoOutClick)

        assertFalse(stateNow(viewModel).isReturnTimeOutOfRange)
    }

    @Test
    fun `a money limit of zero or text cannot be confirmed`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        fillForm(viewModel)

        viewModel.onIntent(PreOutingIntent.OnMoneyChange("0"))
        assertFalse(stateNow(viewModel).canConfirmOuting)
        viewModel.onIntent(PreOutingIntent.OnMoneyChange("abc"))

        assertFalse(stateNow(viewModel).canConfirmOuting)
    }

    @Test
    fun `an incomplete form records nothing`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        viewModel.onIntent(PreOutingIntent.OnGoOutClick)

        viewModel.onIntent(PreOutingIntent.OnConfirmOutingClick)
        advanceUntilIdle()

        assertEquals(PreOutingDecisionState.Undecided, stateNow(viewModel).decision)
    }

    @Test
    fun `the return time picker opens and closes`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        viewModel.onIntent(PreOutingIntent.OnGoOutClick)

        viewModel.onIntent(PreOutingIntent.OnReturnTimeClick)
        assertTrue(stateNow(viewModel).form.isTimePickerOpen)
        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(2, 0))

        assertFalse(stateNow(viewModel).form.isTimePickerOpen)
        assertEquals("02:00", stateNow(viewModel).returnTimeLabel)
    }

    @Test
    fun `the picker starts at 02-00 until a return time is chosen`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        assertEquals(LocalTime(2, 0), stateNow(viewModel).form.pickerTime)

        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(1, 30))

        assertEquals(LocalTime(1, 30), stateNow(viewModel).form.pickerTime)
    }
}
