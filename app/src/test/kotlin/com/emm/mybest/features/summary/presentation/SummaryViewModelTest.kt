package com.emm.mybest.features.summary.presentation

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.LimitsKept
import com.emm.mybest.domain.models.WeekDebt
import com.emm.mybest.domain.models.WeekMoney
import com.emm.mybest.domain.models.WeekOutings
import com.emm.mybest.domain.models.WeekRecovery
import com.emm.mybest.domain.models.WeeklySummary
import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.domain.usecase.summary.ObserveWeeklySummaryUseCase
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val FRIDAY_EVENING = LocalDateTime(2026, 10, 23, 20, 0)

private val EMPTY_SUMMARY = WeeklySummary(
    outings = WeekOutings(0, Money.ZERO, 0, LimitsKept(0, 0)),
    recovery = WeekRecovery(emptyList(), null, emptyList()),
    checks = emptyMap(),
    weighInTrend = WeighInTrend(),
    money = WeekMoney(emptyList(), Money.ZERO, WeekDebt(Money.ZERO, 0, 0), null),
)

class SummaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeSummary = mockk<ObserveWeeklySummaryUseCase>()

    private fun viewModel(): SummaryViewModel {
        every { observeSummary(any()) } returns flowOf(EMPTY_SUMMARY)
        val clock = AppDayClock(FixedClock(FRIDAY_EVENING.toInstant(LIMA)), LIMA)
        return SummaryViewModel(observeSummary, clock)
    }

    private fun TestScope.started(viewModel: SummaryViewModel): SummaryViewModel {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `opens on the current week with no next week`() = runTest {
        val state = started(viewModel()).state.value

        assertEquals("19 OCT – 25 OCT", state.weekLabel)
        assertFalse(state.canGoNext)
    }

    @Test
    fun `previous week shows the week before and allows going back to the next`() = runTest {
        val viewModel = started(viewModel())

        viewModel.onIntent(SummaryIntent.OnPreviousWeekClick)
        advanceUntilIdle()

        assertEquals("12 OCT – 18 OCT", viewModel.state.value.weekLabel)
        assertTrue(viewModel.state.value.canGoNext)
        verify { observeSummary(LocalDate(2026, 10, 16)) }
    }

    @Test
    fun `next week returns to the current week and disables next`() = runTest {
        val viewModel = started(viewModel())
        viewModel.onIntent(SummaryIntent.OnPreviousWeekClick)
        advanceUntilIdle()

        viewModel.onIntent(SummaryIntent.OnNextWeekClick)
        advanceUntilIdle()

        assertEquals("19 OCT – 25 OCT", viewModel.state.value.weekLabel)
        assertFalse(viewModel.state.value.canGoNext)
    }

    @Test
    fun `next week on the current week stays on the current week`() = runTest {
        val viewModel = started(viewModel())

        viewModel.onIntent(SummaryIntent.OnNextWeekClick)
        advanceUntilIdle()

        assertEquals("19 OCT – 25 OCT", viewModel.state.value.weekLabel)
    }
}
