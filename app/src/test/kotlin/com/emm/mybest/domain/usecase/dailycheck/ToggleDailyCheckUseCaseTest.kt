package com.emm.mybest.domain.usecase.dailycheck

import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.testing.FakeDailyCheckRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)

class ToggleDailyCheckUseCaseTest {

    @Test
    fun `an unrecorded check becomes done`() = runTest {
        val repository = FakeDailyCheckRepository()

        ToggleDailyCheckUseCase(repository)(FRIDAY, DailyCheckType.GYM)

        assertEquals(listOf(DailyCheck(FRIDAY, DailyCheckType.GYM, isDone = true)), repository.writes)
    }

    @Test
    fun `a done check becomes not done`() = runTest {
        val repository = FakeDailyCheckRepository(listOf(DailyCheck(FRIDAY, DailyCheckType.DIET, isDone = true)))

        ToggleDailyCheckUseCase(repository)(FRIDAY, DailyCheckType.DIET)

        assertEquals(listOf(DailyCheck(FRIDAY, DailyCheckType.DIET, isDone = false)), repository.writes)
    }

    @Test
    fun `toggling twice returns to the first state`() = runTest {
        val repository = FakeDailyCheckRepository()
        val toggle = ToggleDailyCheckUseCase(repository)

        toggle(FRIDAY, DailyCheckType.ENGLISH)
        toggle(FRIDAY, DailyCheckType.ENGLISH)

        assertEquals(listOf(true, false), repository.writes.map { it.isDone })
    }

    @Test
    fun `toggling one type leaves the others untouched`() = runTest {
        val repository = FakeDailyCheckRepository(listOf(DailyCheck(FRIDAY, DailyCheckType.GYM, isDone = true)))

        ToggleDailyCheckUseCase(repository)(FRIDAY, DailyCheckType.ALCOHOL)

        assertEquals(listOf(DailyCheckType.ALCOHOL), repository.writes.map { it.type })
    }
}
