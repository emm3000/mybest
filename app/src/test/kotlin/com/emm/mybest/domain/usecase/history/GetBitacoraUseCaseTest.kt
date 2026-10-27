package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.models.GoalContribution
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
import com.emm.mybest.domain.usecase.history.BitacoraEntry.ContributionEntry
import com.emm.mybest.domain.usecase.history.BitacoraEntry.InstallmentEntry
import com.emm.mybest.domain.usecase.history.BitacoraEntry.OutingEntry
import com.emm.mybest.domain.usecase.history.BitacoraEntry.PhotosEntry
import com.emm.mybest.domain.usecase.history.BitacoraEntry.ReturnEntry
import com.emm.mybest.domain.usecase.history.BitacoraEntry.StayedInEntry
import com.emm.mybest.domain.usecase.history.BitacoraEntry.WeighInEntry
import com.emm.mybest.domain.usecase.history.BitacoraEntry.WonNightEntry
import com.emm.mybest.domain.usecase.relapse.ObserveRelapsesUseCase
import com.emm.mybest.domain.usecase.relapse.ObserveReturnTimesUseCase
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.FixedClock
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
import org.junit.Assert.assertTrue
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val TODAY_NOON = LocalDateTime(2026, 11, 10, 12, 0).toInstant(LIMA)
private val NOVEMBER = YearMonthValue(2026, 11)
private val OCTOBER = YearMonthValue(2026, 10)
private val LIMITS = Limits(money = Money(10_000), drinks = 3, returnTime = LocalTime(1, 0))

private fun day(month: Int, dayOfMonth: Int) = LocalDate(2026, month, dayOfMonth)

private fun checkIn(spent: Long, mood: Int) = CheckIn(Money(spent), drinks = 2, returnTime = null, mood = mood)

private fun planned(id: Long, appDay: LocalDate, checkIn: CheckIn? = null) =
    Outing(id, appDay, OutingKind.PLANNED, LIMITS, checkIn)

private fun unplanned(id: Long, appDay: LocalDate, checkIn: CheckIn? = null) =
    Outing(id, appDay, OutingKind.UNPLANNED, limits = null, checkIn = checkIn)

private fun photo(date: LocalDate, type: PhotoType, createdAt: Long) =
    ProgressPhoto("p$createdAt", date, type, "/tmp/$createdAt.jpg", createdAt)

private fun check(appDay: LocalDate, type: DailyCheckType, isDone: Boolean = true) =
    DailyCheck(appDay, type, isDone)

private data class Records(
    val outings: List<Outing> = emptyList(),
    val wonNights: List<WonNight> = emptyList(),
    val stayedInDays: Set<LocalDate> = emptySet(),
    val checks: List<DailyCheck> = emptyList(),
    val weights: List<WeightEntry> = emptyList(),
    val photos: List<ProgressPhoto> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val payments: List<DebtPayment> = emptyList(),
    val contributions: List<GoalContribution> = emptyList(),
)

private fun useCase(records: Records): GetBitacoraUseCase {
    val weightRepository = mockk<WeightRepository>()
    every { weightRepository.getWeightProgress() } returns flowOf(records.weights)
    val photoRepository = mockk<PhotoRepository>()
    every { photoRepository.getAllPhotos() } returns flowOf(records.photos)
    val outingRepository = FakeOutingRepository(records.outings, records.wonNights, records.stayedInDays)
    val checkRepository = FakeDailyCheckRepository(records.checks)
    val observeRelapses = ObserveRelapsesUseCase(outingRepository, FakeSetupRepository())
    val appDayClock = AppDayClock(FixedClock(TODAY_NOON), LIMA)
    return GetBitacoraUseCase(
        BitacoraSources(
            body = HistoryBodySources(weightRepository, photoRepository),
            nights = HistoryNightSources(outingRepository, observeRelapses),
            observeReturnTimes = ObserveReturnTimesUseCase(observeRelapses, checkRepository, appDayClock),
            money = BitacoraMoneySources(
                FakeDebtRepository(records.debts, records.payments),
                FakePurchaseGoalRepository(initialContributions = records.contributions),
            ),
        ),
    )
}

private fun fxC() = Records(
    outings = listOf(
        planned(1, day(11, 4), checkIn(9_000, mood = 4)),
        planned(2, day(11, 6), checkIn(15_000, mood = 2)),
        unplanned(3, day(11, 9), checkIn(20_000, mood = 3)),
        planned(4, day(10, 31)),
    ),
    wonNights = listOf(WonNight(day(11, 7), Money(10_000))),
    stayedInDays = setOf(day(11, 1)),
    checks = listOf(
        check(day(11, 2), DailyCheckType.GYM),
        check(day(11, 3), DailyCheckType.GYM),
        check(day(11, 3), DailyCheckType.DIET),
        check(day(11, 7), DailyCheckType.DIET),
        check(day(11, 7), DailyCheckType.ALCOHOL, isDone = false),
        check(day(11, 6), DailyCheckType.ALCOHOL),
    ),
    weights = listOf(
        WeightEntry("w1", day(11, 2), 80.4f),
        WeightEntry("w2", day(11, 8), 79.9f),
        WeightEntry("w3", day(12, 1), 79.0f),
    ),
    debts = listOf(BANCO),
    payments = listOf(
        DebtPayment(BANCO.id, NOVEMBER, Money(40_000)),
        DebtPayment(BANCO.id, OCTOBER, Money(40_000)),
    ),
    contributions = listOf(GoalContribution(1, day(11, 5), Money(10_000))),
)

private val BANCO = Debt(id = 1, name = "Banco", balance = Money(160_000), installment = Money(40_000), paymentDay = 15)

class GetBitacoraUseCaseTest {

    private suspend fun days(records: Records, month: YearMonthValue = NOVEMBER) = useCase(records)(month).first()

    @Test
    fun `days come out newest first and only those with an entry`() = runTest {
        val appDays = days(fxC()).map { it.appDay }

        val expected = listOf(15, 9, 8, 7, 6, 5, 4, 2, 1).map { day(11, it) }
        assertEquals(expected, appDays)
    }

    @Test
    fun `a won night comes before the return of the same day`() = runTest {
        val entries = days(fxC()).first { it.appDay == day(11, 7) }.entries

        assertEquals(listOf(WonNightEntry(WonNight(day(11, 7), Money(10_000))), ReturnEntry(1)), entries)
    }

    @Test
    fun `outings carry their relapse reasons`() = runTest {
        val result = days(fxC()).associate { it.appDay to it.entries }

        assertEquals(
            setOf(RelapseReason.BROKE_LIMIT),
            (result.getValue(day(11, 6)).single() as OutingEntry).relapseReasons,
        )
        assertEquals(
            setOf(RelapseReason.UNPLANNED),
            (result.getValue(day(11, 9)).single() as OutingEntry).relapseReasons,
        )
        assertTrue((result.getValue(day(11, 4)).single() as OutingEntry).relapseReasons.isEmpty())
    }

    @Test
    fun `money, answers and weigh-ins land on their own days`() = runTest {
        val result = days(fxC()).associate { it.appDay to it.entries }

        assertEquals(listOf(InstallmentEntry("Banco", Money(40_000))), result.getValue(day(11, 15)))
        assertEquals(listOf(ContributionEntry(Money(10_000))), result.getValue(day(11, 5)))
        assertEquals(listOf(StayedInEntry), result.getValue(day(11, 1)))
        assertEquals(listOf(WeighInEntry(WeightEntry("w1", day(11, 2), 80.4f))), result.getValue(day(11, 2)))
    }

    @Test
    fun `records outside the month are left out`() = runTest {
        val appDays = days(fxC()).map { it.appDay }

        assertTrue(day(10, 31) !in appDays)
        assertTrue(day(12, 1) !in appDays)
        assertTrue(appDays.all { YearMonthValue.from(it) == NOVEMBER })
    }

    @Test
    fun `an installment of a debt due on the 31st lands on the last day of November`() = runTest {
        val debt = BANCO.copy(paymentDay = 31)
        val records = Records(debts = listOf(debt), payments = listOf(DebtPayment(debt.id, NOVEMBER, Money(40_000))))

        assertEquals(listOf(day(11, 30)), days(records).map { it.appDay })
    }

    @Test
    fun `a return belongs to the month it happens in`() = runTest {
        val records = Records(
            outings = listOf(unplanned(1, day(10, 30))),
            checks = listOf(check(day(11, 2), DailyCheckType.DIET)),
        )

        assertEquals(listOf(ReturnEntry(3)), days(records).single { it.appDay == day(11, 2) }.entries)
        assertTrue(days(records, OCTOBER).none { day -> day.entries.any { it is ReturnEntry } })
    }

    @Test
    fun `a relapse without a return yet gives no return entry`() = runTest {
        val records = Records(outings = listOf(unplanned(1, day(11, 9))))

        assertTrue(days(records).none { day -> day.entries.any { it is ReturnEntry } })
    }

    @Test
    fun `photos of one date make one entry in photo type order`() = runTest {
        val records = Records(
            photos = listOf(photo(day(11, 3), PhotoType.FACE, 1), photo(day(11, 3), PhotoType.TRUNK, 2)),
        )

        assertEquals(listOf(PhotosEntry(listOf(PhotoType.TRUNK, PhotoType.FACE))), days(records).single().entries)
    }

    @Test
    fun `a month with only daily checks gives nothing`() = runTest {
        val records = Records(checks = listOf(check(day(11, 3), DailyCheckType.GYM)))

        assertTrue(days(records).isEmpty())
    }

    @Test
    fun `an empty month gives nothing`() = runTest {
        assertTrue(days(Records()).isEmpty())
    }
}
