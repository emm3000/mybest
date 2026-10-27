package com.emm.mybest.features.history.presentation

import com.emm.mybest.R
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.LimitKind
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.history.GetLogbookUseCase
import com.emm.mybest.domain.usecase.history.HistoryResult
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.weight.DeleteWeightByDateUseCase
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import com.emm.mybest.ui.components.LimitOutcomeState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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

class HistoryViewModelDayStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lima = TimeZone.of("America/Lima")
    private val today = LocalDate(2026, 10, 24)
    private val yesterday = LocalDate(2026, 10, 23)
    private val clock = AppDayClock(FixedClock(LocalDateTime(2026, 10, 24, 12, 0).toInstant(lima)), lima)

    private val limits = Limits(money = Money(20_000), drinks = 4, returnTime = LocalTime(1, 0))
    private val brokenCheckIn = CheckIn(totalSpent = Money(26_000), drinks = 5, returnTime = LocalTime(1, 0), mood = 2)
    private val keptCheckIn = CheckIn(totalSpent = Money(10_000), drinks = 2, returnTime = LocalTime(0, 30), mood = 4)

    private fun outing(appDay: LocalDate, checkIn: CheckIn?) =
        Outing(id = 1, appDay = appDay, kind = OutingKind.PLANNED, limits = limits, checkIn = checkIn)

    private fun emptyResult(vararg summaries: DaySummary) = HistoryResult(
        monthlyData = summaries.associateBy { it.date },
        monthWeightCount = 0,
        monthPhotoCount = 0,
        monthOutingCount = 0,
        monthWonNightCount = 0,
    )

    private fun viewModel(result: HistoryResult): HistoryViewModel {
        val getHistory = mockk<GetHistoryUseCase>()
        every { getHistory(any()) } returns flowOf(result)
        val getLogbook = mockk<GetLogbookUseCase>()
        every { getLogbook(any()) } returns flowOf(emptyList())
        return HistoryViewModel(
            getHistory,
            getLogbook,
            mockk<DeleteWeightByDateUseCase>(),
            mockk<DeletePhotoUseCase>(),
            HistoryNoteUseCases(mockk(), mockk(), mockk()),
            clock,
        )
    }

    private suspend fun loaded(result: HistoryResult): HistoryState =
        viewModel(result).state.first { !it.isLoading }

    private suspend fun selected(result: HistoryResult, date: LocalDate): DayDetailState {
        val viewModel = viewModel(result)
        viewModel.onIntent(HistoryIntent.OnDateSelected(date))
        return checkNotNull(viewModel.state.first { it.dayDetail != null }.dayDetail)
    }

    @Test
    fun `month counts include outings and won nights`() = runTest {
        val result = emptyResult().copy(monthOutingCount = 3, monthWonNightCount = 2)

        val state = loaded(result)

        assertEquals(3, state.monthOutingCount)
        assertEquals(2, state.monthWonNightCount)
    }

    @Test
    fun `a relapse is marked as a relapse and not as a plain outing`() = runTest {
        val summary = DaySummary(
            date = today,
            outing = outing(today, brokenCheckIn),
            relapseReasons = setOf(RelapseReason.BROKE_LIMIT),
        )

        val marks = loaded(emptyResult(summary)).dayMarks.getValue(today)

        assertEquals(listOf(HistoryMarkKind.RELAPSE), marks.marks)
    }

    @Test
    fun `an outing without relapse reasons is marked as a plain outing`() = runTest {
        val summary = DaySummary(date = today, outing = outing(today, keptCheckIn))

        val marks = loaded(emptyResult(summary)).dayMarks.getValue(today)

        assertEquals(listOf(HistoryMarkKind.OUTING), marks.marks)
    }

    @Test
    fun `a day marks its won night, weigh-in and photo in a fixed order`() = runTest {
        val summary = DaySummary(
            date = today,
            weight = WeightEntry(id = "w", date = today, weight = 78.2f),
            photos = listOf(ProgressPhoto("p", today, PhotoType.FACE, "/tmp/face.jpg", 1L)),
            wonNight = WonNight(today, estimatedCost = null),
        )

        val marks = loaded(emptyResult(summary)).dayMarks.getValue(today)

        assertEquals(
            listOf(HistoryMarkKind.WON_NIGHT, HistoryMarkKind.WEIGHT, HistoryMarkKind.PHOTO),
            marks.marks,
        )
    }

    @Test
    fun `a day marks how many daily checks were done`() = runTest {
        val summary = DaySummary(
            date = today,
            checks = listOf(
                DailyCheck(today, DailyCheckType.GYM, isDone = true),
                DailyCheck(today, DailyCheckType.DIET, isDone = true),
                DailyCheck(today, DailyCheckType.ENGLISH, isDone = false),
            ),
        )

        val marks = loaded(emptyResult(summary)).dayMarks.getValue(today)

        assertEquals(2, marks.checkCount)
        assertTrue(marks.marks.isEmpty())
    }

    @Test
    fun `no day detail while no day is selected`() = runTest {
        assertNull(loaded(emptyResult()).dayDetail)
    }

    @Test
    fun `a day without records shows an empty detail`() = runTest {
        val detail = selected(emptyResult(), today)

        assertTrue(detail.isEmpty)
        assertNull(detail.outing)
        assertTrue(detail.checks.isEmpty())
    }

    @Test
    fun `the relapse day detail shows kind, spend, mood and reasons`() = runTest {
        val summary = DaySummary(
            date = today,
            outing = outing(today, brokenCheckIn),
            relapseReasons = setOf(RelapseReason.OVER_MONTHLY_LIMIT, RelapseReason.BROKE_LIMIT),
        )

        val outing = checkNotNull(selected(emptyResult(summary), today).outing)

        assertEquals(OutingKind.PLANNED, outing.kind)
        assertEquals("S/ 260.00", outing.spentLabel)
        assertEquals(2, outing.mood)
        assertEquals(listOf(RelapseReason.BROKE_LIMIT, RelapseReason.OVER_MONTHLY_LIMIT), outing.relapseReasons)
    }

    @Test
    fun `the relapse day detail shows each limit result`() = runTest {
        val summary = DaySummary(
            date = today,
            outing = outing(today, brokenCheckIn),
            relapseReasons = setOf(RelapseReason.BROKE_LIMIT),
        )

        val outing = checkNotNull(selected(emptyResult(summary), today).outing)

        assertEquals(
            listOf(
                LimitOutcomeState.MoneyExceeded("S/ 60.00"),
                LimitOutcomeState.DrinksExceeded(1),
                LimitOutcomeState.Kept(LimitKind.RETURN_TIME),
            ),
            outing.outcomes,
        )
    }

    @Test
    fun `an outing pending its check-in shows no spend, mood or limit results`() = runTest {
        val summary = DaySummary(date = today, outing = outing(today, checkIn = null))

        val outing = checkNotNull(selected(emptyResult(summary), today).outing)

        assertTrue(outing.isPendingCheckIn)
        assertNull(outing.spentLabel)
        assertNull(outing.mood)
        assertTrue(outing.outcomes.isEmpty())
    }

    @Test
    fun `a won night detail carries its estimated amount`() = runTest {
        val summary = DaySummary(date = today, wonNight = WonNight(today, estimatedCost = Money(26_000)))

        val wonNight = selected(emptyResult(summary), today).wonNight

        assertEquals(WonNightDetailState.Estimated("S/ 260.00"), wonNight)
    }

    @Test
    fun `a won night with no estimate is unestimated`() = runTest {
        val summary = DaySummary(date = today, wonNight = WonNight(today, estimatedCost = null))

        assertEquals(WonNightDetailState.Unestimated, selected(emptyResult(summary), today).wonNight)
    }

    @Test
    fun `the day detail lists the four daily checks and answers unrecorded ones with a dash`() = runTest {
        val summary = DaySummary(
            date = today,
            checks = listOf(DailyCheck(today, DailyCheckType.DIET, isDone = true)),
        )

        val checks = selected(emptyResult(summary), today).checks

        assertEquals(DailyCheckType.entries, checks.map { it.type })
        assertEquals(
            listOf(CheckAnswer.UNRECORDED, CheckAnswer.YES, CheckAnswer.UNRECORDED, CheckAnswer.UNRECORDED),
            checks.map { it.answer },
        )
    }

    @Test
    fun `a check recorded as not done answers no`() = runTest {
        val summary = DaySummary(
            date = today,
            checks = listOf(
                DailyCheck(today, DailyCheckType.GYM, isDone = true),
                DailyCheck(today, DailyCheckType.ALCOHOL, isDone = false),
            ),
        )

        val checks = selected(emptyResult(summary), today).checks

        assertEquals(CheckAnswer.NO, checks.single { it.type == DailyCheckType.ALCOHOL }.answer)
    }

    @Test
    fun `the day detail labels the weigh-in with its note`() = runTest {
        val weight = WeightEntry(id = "w", date = today, weight = 78.24f, note = "after gym")
        val summary = DaySummary(date = today, weight = weight)

        assertEquals("78.2 kg · after gym", selected(emptyResult(summary), today).weight?.label)
    }

    @Test
    fun `only the weigh-in of the current app day can be deleted`() = runTest {
        val result = emptyResult(
            DaySummary(date = yesterday, weight = WeightEntry(id = "w1", date = yesterday, weight = 78f)),
            DaySummary(date = today, weight = WeightEntry(id = "w2", date = today, weight = 78f)),
        )

        assertFalse(checkNotNull(selected(result, yesterday).weight).canDelete)
        assertTrue(checkNotNull(selected(result, today).weight).canDelete)
    }

    @Test
    fun `only the photos of the current app day can be deleted`() = runTest {
        val old = ProgressPhoto("p1", yesterday, PhotoType.FACE, "/tmp/old.jpg", 1L)
        val recent = ProgressPhoto("p2", today, PhotoType.FACE, "/tmp/recent.jpg", 2L)
        val result = emptyResult(
            DaySummary(date = yesterday, photos = listOf(old)),
            DaySummary(date = today, photos = listOf(recent)),
        )

        val faceDescription = R.string.history_photo_face_description

        assertEquals(listOf(PhotoRowState(old, false, faceDescription)), selected(result, yesterday).photos)
        assertEquals(listOf(PhotoRowState(recent, true, faceDescription)), selected(result, today).photos)
    }

    @Test
    fun `each photo row describes its photo type`() = runTest {
        val trunk = ProgressPhoto("p1", today, PhotoType.TRUNK, "/tmp/trunk.jpg", 1L)
        val face = ProgressPhoto("p2", today, PhotoType.FACE, "/tmp/face.jpg", 2L)
        val result = emptyResult(DaySummary(date = today, photos = listOf(trunk, face)))

        val descriptions = selected(result, today).photos.map { it.descriptionRes }

        assertEquals(
            listOf(R.string.history_photo_trunk_description, R.string.history_photo_face_description),
            descriptions,
        )
    }

    @Test
    fun `the month label names the neighbouring months`() = runTest {
        val state = loaded(emptyResult())

        assertEquals("← SEP · NOV →", state.monthNavigationLabel)
    }

    @Test
    fun `the month label wraps over the year`() = runTest {
        val viewModel = viewModel(emptyResult())
        val january = YearMonthValue(2026, 1)

        viewModel.onIntent(HistoryIntent.OnMonthChange(january))
        val state = viewModel.state.first { it.selectedMonth == january && !it.isLoading }

        assertEquals("← DIC · FEB →", state.monthNavigationLabel)
    }
}
