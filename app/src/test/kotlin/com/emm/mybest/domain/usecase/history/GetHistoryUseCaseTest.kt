package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.domain.usecase.relapse.ObserveRelapsesUseCase
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakeSetupRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetHistoryUseCaseTest {

    private val october = YearMonthValue(2026, 10)
    private val november = YearMonthValue(2026, 11)

    private val october31 = LocalDate(2026, 10, 31)
    private val october24 = LocalDate(2026, 10, 24)
    private val october10 = LocalDate(2026, 10, 10)
    private val october7 = LocalDate(2026, 10, 7)
    private val september30 = LocalDate(2026, 9, 30)

    private val weight1 = WeightEntry(id = "w1", date = october10, weight = 78.2f)
    private val weight2 = WeightEntry(id = "w2", date = october7, weight = 78.5f)
    private val outOfMonthWeight = WeightEntry(id = "wout", date = september30, weight = 80f)
    private val photo = ProgressPhoto(
        id = "p1",
        date = october7,
        type = PhotoType.TRUNK,
        photoPath = "/tmp/trunk.jpg",
        createdAt = 1L,
    )

    private val limits = Limits(money = Money(20_000), drinks = 4, returnTime = LocalTime(1, 0))
    private val checkIn = CheckIn(totalSpent = Money(26_000), drinks = 5, returnTime = LocalTime(2, 0), mood = 3)

    private fun plannedOuting(appDay: LocalDate, checkIn: CheckIn? = null) =
        Outing(id = 1, appDay = appDay, kind = OutingKind.PLANNED, limits = limits, checkIn = checkIn)

    private fun unplannedOuting(appDay: LocalDate) =
        Outing(id = 2, appDay = appDay, kind = OutingKind.UNPLANNED, limits = null, checkIn = null)

    private fun useCase(fixture: HistoryFixture = HistoryFixture()): GetHistoryUseCase {
        val weightRepository = mockk<WeightRepository>()
        every { weightRepository.getWeightProgress() } returns flowOf(fixture.weights)
        val photoRepository = mockk<PhotoRepository>()
        every { photoRepository.getAllPhotos() } returns flowOf(fixture.photos)
        val outingRepository = FakeOutingRepository(fixture.outings, fixture.wonNights)
        return GetHistoryUseCase(
            body = HistoryBodySources(weightRepository, photoRepository),
            nights = HistoryNightSources(
                outingRepository,
                ObserveRelapsesUseCase(outingRepository, FakeSetupRepository()),
            ),
            dailyCheckRepository = FakeDailyCheckRepository(fixture.checks),
        )
    }

    @Test
    fun `empty data yields an empty month`() = runTest {
        val result = useCase()(october).first()

        assertTrue(result.monthlyData.isEmpty())
        assertTrue(result.recentEntries.isEmpty())
        assertEquals(0, result.monthOutingCount)
        assertEquals(0, result.monthWonNightCount)
    }

    @Test
    fun `weight and photo on the same date share one summary`() = runTest {
        val result = useCase(HistoryFixture(weights = listOf(weight2), photos = listOf(photo)))(october).first()

        val summary = result.monthlyData.getValue(october7)
        assertEquals(weight2, summary.weight)
        assertEquals(listOf(photo), summary.photos)
    }

    @Test
    fun `weight and photo counts only include the month`() = runTest {
        val result = useCase(
            HistoryFixture(weights = listOf(weight1, weight2, outOfMonthWeight), photos = listOf(photo)),
        )(october)
            .first()

        assertEquals(2, result.monthWeightCount)
        assertEquals(1, result.monthPhotoCount)
    }

    @Test
    fun `recent entries list weigh-ins and photos newest first`() = runTest {
        val result = useCase(
            HistoryFixture(weights = listOf(weight2, weight1), photos = listOf(photo)),
        )(october).first()

        assertEquals(listOf(october10, october7), result.recentEntries.map { it.date })
        assertEquals(78.5f, result.recentEntries[1].weight)
        assertEquals(listOf(PhotoType.TRUNK), result.recentEntries[1].photoTypes)
    }

    @Test
    fun `recent entries skip days with only an outing or checks`() = runTest {
        val fixture = HistoryFixture(
            outings = listOf(plannedOuting(october24)),
            checks = listOf(DailyCheck(october10, DailyCheckType.GYM, isDone = true)),
        )

        val result = useCase(fixture)(october).first()

        assertTrue(result.recentEntries.isEmpty())
    }

    @Test
    fun `an outing keyed to the 31st lands on the 31st even when it ended on the 1st at 02_00`() = runTest {
        val outing = plannedOuting(october31, checkIn)
        val useCase = useCase(HistoryFixture(outings = listOf(outing)))

        val octoberResult = useCase(october).first()
        val novemberResult = useCase(november).first()

        assertEquals(outing, octoberResult.monthlyData.getValue(october31).outing)
        assertEquals(1, octoberResult.monthOutingCount)
        assertNull(novemberResult.monthlyData[LocalDate(2026, 11, 1)])
        assertEquals(0, novemberResult.monthOutingCount)
    }

    @Test
    fun `relapse reasons come from the relapse use case`() = runTest {
        val result = useCase(HistoryFixture(outings = listOf(unplannedOuting(october24))))(october).first()

        val summary = result.monthlyData.getValue(october24)
        assertTrue(summary.isRelapse)
        assertEquals(setOf(RelapseReason.UNPLANNED), summary.relapseReasons)
    }

    @Test
    fun `a planned outing that broke a limit carries its broken limit reason`() = runTest {
        val result = useCase(HistoryFixture(outings = listOf(plannedOuting(october24, checkIn))))(october).first()

        assertEquals(setOf(RelapseReason.BROKE_LIMIT), result.monthlyData.getValue(october24).relapseReasons)
    }

    @Test
    fun `an outing that kept every limit is not a relapse`() = runTest {
        val kept = CheckIn(totalSpent = Money(10_000), drinks = 2, returnTime = LocalTime(23, 0), mood = 4)

        val result = useCase(HistoryFixture(outings = listOf(plannedOuting(october24, kept))))(october).first()

        val summary = result.monthlyData.getValue(october24)
        assertTrue(summary.hasOuting)
        assertFalse(summary.isRelapse)
    }

    @Test
    fun `a won night marks its day and counts in the month`() = runTest {
        val wonNight = WonNight(appDay = october24, estimatedCost = Money(26_000))

        val result = useCase(HistoryFixture(wonNights = listOf(wonNight)))(october).first()

        assertEquals(wonNight, result.monthlyData.getValue(october24).wonNight)
        assertEquals(1, result.monthWonNightCount)
    }

    @Test
    fun `a day with only checks has activity`() = runTest {
        val checks = listOf(
            DailyCheck(october10, DailyCheckType.GYM, isDone = true),
            DailyCheck(october10, DailyCheckType.DIET, isDone = true),
            DailyCheck(october10, DailyCheckType.ENGLISH, isDone = false),
        )

        val summary = useCase(HistoryFixture(checks = checks))(october).first().monthlyData.getValue(october10)

        assertTrue(summary.hasActivity)
        assertEquals(2, summary.doneCheckCount)
    }

    @Test
    fun `a drank alcohol check does not count`() = runTest {
        val checks = listOf(
            DailyCheck(october10, DailyCheckType.GYM, isDone = true),
            DailyCheck(october10, DailyCheckType.ALCOHOL, isDone = true),
        )

        val summary = useCase(HistoryFixture(checks = checks))(october).first().monthlyData.getValue(october10)

        assertEquals(1, summary.doneCheckCount)
    }

    @Test
    fun `an alcohol check marked as not drank counts`() = runTest {
        val checks = listOf(DailyCheck(october10, DailyCheckType.ALCOHOL, isDone = false))

        val summary = useCase(HistoryFixture(checks = checks))(october).first().monthlyData.getValue(october10)

        assertEquals(1, summary.doneCheckCount)
        assertTrue(summary.hasActivity)
    }

    @Test
    fun `a day with only a drank alcohol check has no activity`() = runTest {
        val checks = listOf(DailyCheck(october10, DailyCheckType.ALCOHOL, isDone = true))

        val result = useCase(HistoryFixture(checks = checks))(october).first()

        assertFalse(result.monthlyData[october10]?.hasActivity ?: false)
    }

    @Test
    fun `a day whose checks are all undone has no activity`() = runTest {
        val checks = listOf(DailyCheck(october10, DailyCheckType.GYM, isDone = false))

        val result = useCase(HistoryFixture(checks = checks))(october).first()

        assertFalse(result.monthlyData[october10]?.hasActivity ?: false)
    }

    @Test
    fun `a summary with no records has no activity`() {
        assertFalse(DaySummary(date = october10).hasActivity)
    }
}

private data class HistoryFixture(
    val weights: List<WeightEntry> = emptyList(),
    val photos: List<ProgressPhoto> = emptyList(),
    val outings: List<Outing> = emptyList(),
    val wonNights: List<WonNight> = emptyList(),
    val checks: List<DailyCheck> = emptyList(),
)
