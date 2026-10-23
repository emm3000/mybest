package com.emm.mybest.domain.usecase.relapse

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.ReturnTime
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)
private val SUNDAY = LocalDate(2026, 10, 25)
private val MONDAY = LocalDate(2026, 10, 26)

class ObserveReturnTimesUseCaseTest {

    private fun unplanned(id: Long, day: LocalDate) = Outing(id, day, OutingKind.UNPLANNED, null, null)

    private fun dietDone(day: LocalDate) = DailyCheck(day, DailyCheckType.DIET, isDone = true)

    private fun drank(day: LocalDate) = DailyCheck(day, DailyCheckType.ALCOHOL, isDone = true)

    private suspend fun returnTimes(
        relapseDays: List<LocalDate>,
        checks: List<DailyCheck>,
        today: LocalDate = MONDAY,
    ): List<ReturnTime> {
        val outings = relapseDays.mapIndexed { index, day -> unplanned(index + 1L, day) }
        val clock = FixedClock(LocalDateTime(today, LocalTime(21, 0)).toInstant(LIMA))
        return ObserveReturnTimesUseCase(
            ObserveRelapsesUseCase(FakeOutingRepository(outings), FakeSetupRepository()),
            FakeDailyCheckRepository(checks),
            AppDayClock(clock, LIMA),
        ).invoke().first()
    }

    @Test
    fun `returns the next day when diet is done and there is no alcohol mark`() = runTest {
        val result = returnTimes(listOf(FRIDAY), listOf(dietDone(SATURDAY)))

        assertEquals(listOf(ReturnTime(FRIDAY, SATURDAY, days = 1)), result)
    }

    @Test
    fun `a day with alcohol does not return even with diet done`() = runTest {
        val checks = listOf(dietDone(SATURDAY), drank(SATURDAY), dietDone(SUNDAY))

        val result = returnTimes(listOf(FRIDAY), checks)

        assertEquals(listOf(ReturnTime(FRIDAY, SUNDAY, days = 2)), result)
    }

    @Test
    fun `an alcohol mark set to not drank does not block the return`() = runTest {
        val checks = listOf(dietDone(SATURDAY), DailyCheck(SATURDAY, DailyCheckType.ALCOHOL, isDone = false))

        val result = returnTimes(listOf(FRIDAY), checks)

        assertEquals(listOf(ReturnTime(FRIDAY, SATURDAY, days = 1)), result)
    }

    @Test
    fun `stays open while no day qualifies`() = runTest {
        val result = returnTimes(listOf(FRIDAY), listOf(drank(SATURDAY)))

        assertEquals(listOf(ReturnTime(FRIDAY, returnAppDay = null, days = null)), result)
    }

    @Test
    fun `the relapse day itself never counts as the return`() = runTest {
        val result = returnTimes(listOf(FRIDAY), listOf(dietDone(FRIDAY)))

        assertEquals(listOf(ReturnTime(FRIDAY, returnAppDay = null, days = null)), result)
    }

    @Test
    fun `two relapses share the same return day and come newest first`() = runTest {
        val result = returnTimes(listOf(FRIDAY, SATURDAY), listOf(dietDone(SUNDAY)))

        assertEquals(
            listOf(ReturnTime(SATURDAY, SUNDAY, days = 1), ReturnTime(FRIDAY, SUNDAY, days = 2)),
            result,
        )
    }
}
