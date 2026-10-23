package com.emm.mybest.domain.usecase.dailycheck

import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.testing.FakeDailyCheckRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)

class RecordDailyCheckUseCaseTest {

    @Test
    fun `records an unmarked check as not done`() = runTest {
        val repository = FakeDailyCheckRepository()

        RecordDailyCheckUseCase(repository)(FRIDAY, DailyCheckType.ALCOHOL, isDone = false)

        assertEquals(listOf(DailyCheck(FRIDAY, DailyCheckType.ALCOHOL, isDone = false)), repository.writes)
    }

    @Test
    fun `replaces an earlier mark`() = runTest {
        val repository = FakeDailyCheckRepository(listOf(DailyCheck(FRIDAY, DailyCheckType.ALCOHOL, isDone = true)))

        RecordDailyCheckUseCase(repository)(FRIDAY, DailyCheckType.ALCOHOL, isDone = false)

        assertEquals(listOf(DailyCheck(FRIDAY, DailyCheckType.ALCOHOL, isDone = false)), repository.writes)
    }
}
