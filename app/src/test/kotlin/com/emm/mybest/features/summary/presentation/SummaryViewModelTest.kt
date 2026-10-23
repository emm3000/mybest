package com.emm.mybest.features.summary.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import com.emm.mybest.R
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.LimitsKept
import com.emm.mybest.domain.models.ReturnTime
import com.emm.mybest.domain.models.WeekDebt
import com.emm.mybest.domain.models.WeekMoney
import com.emm.mybest.domain.models.WeekOutings
import com.emm.mybest.domain.models.WeekRecovery
import com.emm.mybest.domain.models.WeeklySummary
import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.domain.usecase.dailycheck.WeekProgress
import com.emm.mybest.domain.usecase.summary.ObserveWeeklySummaryUseCase
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
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

    private fun viewModel(summary: WeeklySummary = EMPTY_SUMMARY): SummaryViewModel {
        every { observeSummary(any()) } returns flowOf(summary)
        val clock = AppDayClock(FixedClock(FRIDAY_EVENING.toInstant(LIMA)), LIMA)
        return SummaryViewModel(observeSummary, clock)
    }

    private fun TestScope.started(viewModel: SummaryViewModel): SummaryViewModel {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        return viewModel
    }

    private fun TestScope.sectionOf(summary: WeeklySummary, @StringRes title: Int): SummarySectionState =
        started(viewModel(summary)).state.value.sections.first { it.titleRes == title }

    private fun line(@StringRes label: Int, value: SummaryText, color: Color = AtelierInk) =
        SummaryRowState.Line(SummaryText(label), value, color)

    @Test
    fun `outings section lists count, spend and pending check-ins`() = runTest {
        val outings = WeekOutings(2, Money(260_00), 1, LimitsKept(0, 0))

        val section = sectionOf(EMPTY_SUMMARY.copy(outings = outings), R.string.summary_section_outings)

        assertEquals(
            listOf(
                line(R.string.summary_outings_count, SummaryText.literal("2")),
                line(R.string.summary_outings_spent, SummaryText.literal("S/ 260.00")),
                line(R.string.summary_outings_pending, SummaryText.literal("1")),
            ),
            section.rows,
        )
    }

    @Test
    fun `limits section says how many outings kept every limit`() = runTest {
        val outings = WeekOutings(3, Money.ZERO, 0, LimitsKept(kept = 2, checkedIn = 3))

        val section = sectionOf(EMPTY_SUMMARY.copy(outings = outings), R.string.summary_section_limits)

        assertEquals(
            listOf(line(R.string.summary_limits_label, SummaryText(R.string.summary_limits_kept, listOf(2, 3)))),
            section.rows,
        )
    }

    @Test
    fun `limits section is done when every checked-in outing kept its limits`() = runTest {
        val outings = WeekOutings(2, Money.ZERO, 0, LimitsKept(kept = 2, checkedIn = 2))

        val section = sectionOf(EMPTY_SUMMARY.copy(outings = outings), R.string.summary_section_limits)

        assertEquals(AtelierDone, (section.rows.single() as SummaryRowState.Line).valueColor)
    }

    @Test
    fun `limits section says there is nothing to judge without check-ins`() = runTest {
        val section = sectionOf(EMPTY_SUMMARY, R.string.summary_section_limits)

        assertEquals(
            listOf(line(R.string.summary_limits_label, SummaryText(R.string.summary_limits_none))),
            section.rows,
        )
    }

    private fun recoveryOf(vararg times: ReturnTime) = EMPTY_SUMMARY.copy(
        recovery = WeekRecovery(emptyList(), times.firstOrNull(), times.toList()),
    )

    private fun returned(relapse: LocalDate, days: Int) =
        ReturnTime(relapse, relapse.plus(days, DateTimeUnit.DAY), days)

    @Test
    fun `recovery section says the week had no relapse`() = runTest {
        val section = sectionOf(EMPTY_SUMMARY, R.string.summary_section_recovery)

        assertEquals(
            listOf(line(R.string.summary_recovery_label, SummaryText(R.string.summary_recovery_none))),
            section.rows,
        )
    }

    @Test
    fun `recovery section says how long the return took and lists the history`() = runTest {
        val summary = recoveryOf(returned(LocalDate(2026, 10, 17), days = 2))

        val section = sectionOf(summary, R.string.summary_section_recovery)

        assertEquals(
            listOf(
                line(R.string.summary_recovery_label, SummaryText(R.string.summary_recovery_returned_other, listOf(2))),
                SummaryRowState.Line(
                    SummaryText(R.string.summary_recovery_history_label, listOf("SÁB 17 OCT")),
                    SummaryText(R.string.summary_days_other, listOf(2)),
                    AtelierInk,
                ),
            ),
            section.rows,
        )
    }

    @Test
    fun `recovery section uses the singular for a one day return`() = runTest {
        val section =
            sectionOf(recoveryOf(returned(LocalDate(2026, 10, 17), days = 1)), R.string.summary_section_recovery)

        assertEquals(
            line(R.string.summary_recovery_label, SummaryText(R.string.summary_recovery_returned_one, listOf(1))),
            section.rows.first(),
        )
    }

    @Test
    fun `recovery section counts the days without a return up to today`() = runTest {
        val open = ReturnTime(LocalDate(2026, 10, 21), returnAppDay = null, days = null)

        val section = sectionOf(recoveryOf(open), R.string.summary_section_recovery)

        assertEquals(
            listOf(
                line(R.string.summary_recovery_label, SummaryText(R.string.summary_recovery_open_other, listOf(2))),
                SummaryRowState.Line(
                    SummaryText(R.string.summary_recovery_history_label, listOf("MIÉ 21 OCT")),
                    SummaryText(R.string.summary_recovery_history_open),
                    AtelierInk,
                ),
            ),
            section.rows,
        )
    }

    private fun checkLine(@StringRes label: Int, value: SummaryText, color: Color) =
        SummaryRowState.Line(SummaryText(label), value, color)

    @Test
    fun `checks section lists each check against its target with alcohol as a maximum`() = runTest {
        val checks = mapOf(DailyCheckType.ALCOHOL to WeekProgress(1, 2), DailyCheckType.GYM to WeekProgress(4, 4))

        val section = sectionOf(EMPTY_SUMMARY.copy(checks = checks), R.string.summary_section_checks)

        assertEquals(
            listOf(
                checkLine(
                    R.string.summary_check_gym,
                    SummaryText(R.string.summary_checks_progress, listOf(4, 4)),
                    AtelierDone,
                ),
                checkLine(
                    R.string.summary_check_alcohol,
                    SummaryText(R.string.summary_checks_maximum, listOf(1, 2)),
                    AtelierDone,
                ),
            ),
            section.rows,
        )
    }

    @Test
    fun `checks section is not done below the target or above the alcohol maximum`() = runTest {
        val checks = mapOf(DailyCheckType.DIET to WeekProgress(3, 5), DailyCheckType.ALCOHOL to WeekProgress(3, 2))

        val section = sectionOf(EMPTY_SUMMARY.copy(checks = checks), R.string.summary_section_checks)

        assertEquals(listOf(AtelierInk, AtelierInk), section.rows.map { (it as SummaryRowState.Line).valueColor })
    }

    @Test
    fun `checks section asks to set the weekly targets when there are none`() = runTest {
        val section = sectionOf(EMPTY_SUMMARY, R.string.summary_section_checks)

        assertEquals(listOf(SummaryRowState.SetupPrompt(SummaryText(R.string.summary_checks_setup))), section.rows)
    }

    @Test
    fun `weight section shows the latest weight, the change and what is left to the target`() = runTest {
        val trend = WeighInTrend(latestKg = 92.4f, changeSincePreviousKg = -0.8f, remainingToTargetKg = 1.4f)

        val section = sectionOf(EMPTY_SUMMARY.copy(weighInTrend = trend), R.string.summary_section_weight)

        assertEquals(
            listOf(
                line(R.string.summary_weight_latest, SummaryText(R.string.summary_weight_value, listOf(92.4f))),
                line(R.string.summary_weight_change, SummaryText(R.string.summary_weight_change_down, listOf(0.8f))),
                line(R.string.summary_weight_remaining, SummaryText(R.string.summary_weight_value, listOf(1.4f))),
            ),
            section.rows,
        )
    }

    @Test
    fun `weight section shows only the latest weight without a previous weigh-in or target`() = runTest {
        val section = sectionOf(
            EMPTY_SUMMARY.copy(weighInTrend = WeighInTrend(latestKg = 92.4f)),
            R.string.summary_section_weight,
        )

        assertEquals(
            listOf(line(R.string.summary_weight_latest, SummaryText(R.string.summary_weight_value, listOf(92.4f)))),
            section.rows,
        )
    }

    @Test
    fun `weight section asks for a first weigh-in when there is none`() = runTest {
        val section = sectionOf(EMPTY_SUMMARY, R.string.summary_section_weight)

        assertEquals(listOf(SummaryRowState.SetupPrompt(SummaryText(R.string.summary_weight_setup))), section.rows)
    }

    @Test
    fun `clicking a setup prompt opens the settings`() = runTest {
        val viewModel = started(viewModel())

        viewModel.effect.test {
            viewModel.onIntent(SummaryIntent.OnSetupPromptClick)
            assertEquals(SummaryEffect.OpenSettings, awaitItem())
        }
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
