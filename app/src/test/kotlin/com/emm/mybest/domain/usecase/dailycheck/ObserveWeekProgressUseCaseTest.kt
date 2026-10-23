package com.emm.mybest.domain.usecase.dailycheck

import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeSetupRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val WEDNESDAY = LocalDate(2026, 10, 21)
private val PREVIOUS_SUNDAY = LocalDate(2026, 10, 18)
private val MONDAY = LocalDate(2026, 10, 19)
private val SUNDAY = LocalDate(2026, 10, 25)
private val NEXT_MONDAY = LocalDate(2026, 10, 26)

class ObserveWeekProgressUseCaseTest {

    private fun useCase(
        checks: List<DailyCheck>,
        setup: Setup,
    ) = ObserveWeekProgressUseCase(
        dailyCheckRepository = FakeDailyCheckRepository(checks, isRangeIgnored = true),
        setupRepository = FakeSetupRepository(setup),
    )

    private fun done(day: LocalDate, type: DailyCheckType, isDone: Boolean = true) =
        DailyCheck(day, type, isDone)

    @Test
    fun `counts done days against the weekly target`() = runTest {
        val setup = Setup(weeklyTargets = mapOf(DailyCheckType.GYM to 4))
        val checks = listOf(done(MONDAY, DailyCheckType.GYM), done(WEDNESDAY, DailyCheckType.GYM))

        val progress = useCase(checks, setup)(WEDNESDAY).first()

        assertEquals(WeekProgress(count = 2, target = 4), progress[DailyCheckType.GYM])
    }

    @Test
    fun `counts only days inside the Monday to Sunday app week`() = runTest {
        val setup = Setup(weeklyTargets = mapOf(DailyCheckType.DIET to 5))
        val checks = listOf(
            done(PREVIOUS_SUNDAY, DailyCheckType.DIET),
            done(MONDAY, DailyCheckType.DIET),
            done(SUNDAY, DailyCheckType.DIET),
            done(NEXT_MONDAY, DailyCheckType.DIET),
        )

        val progress = useCase(checks, setup)(WEDNESDAY).first()

        assertEquals(2, progress.getValue(DailyCheckType.DIET).count)
    }

    @Test
    fun `ignores days marked not done`() = runTest {
        val setup = Setup(weeklyTargets = mapOf(DailyCheckType.ENGLISH to 3))
        val checks = listOf(done(MONDAY, DailyCheckType.ENGLISH, isDone = false))

        val progress = useCase(checks, setup)(WEDNESDAY).first()

        assertEquals(0, progress.getValue(DailyCheckType.ENGLISH).count)
    }

    @Test
    fun `alcohol counts the days drank`() = runTest {
        val setup = Setup(weeklyTargets = mapOf(DailyCheckType.ALCOHOL to 2))
        val checks = listOf(
            done(MONDAY, DailyCheckType.ALCOHOL),
            done(WEDNESDAY, DailyCheckType.ALCOHOL, isDone = false),
        )

        val progress = useCase(checks, setup)(WEDNESDAY).first()

        assertEquals(WeekProgress(count = 1, target = 2), progress[DailyCheckType.ALCOHOL])
    }

    @Test
    fun `unset target has no progress`() = runTest {
        val checks = listOf(done(MONDAY, DailyCheckType.GYM))

        val progress = useCase(checks, Setup())(WEDNESDAY).first()

        assertNull(progress[DailyCheckType.GYM])
    }

    @Test
    fun `counts only the type it reports`() = runTest {
        val setup = Setup(weeklyTargets = mapOf(DailyCheckType.GYM to 4))
        val checks = listOf(done(MONDAY, DailyCheckType.DIET))

        val progress = useCase(checks, setup)(WEDNESDAY).first()

        assertEquals(0, progress.getValue(DailyCheckType.GYM).count)
    }
}
