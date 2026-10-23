package com.emm.mybest.domain.usecase.dailycheck

import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.testing.FakeDailyCheckRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)
private val THURSDAY = LocalDate(2026, 10, 22)

class ObserveDailyChecksUseCaseTest {

    @Test
    fun `emits the checks recorded on the app day`() = runTest {
        val repository = FakeDailyCheckRepository(
            listOf(
                DailyCheck(FRIDAY, DailyCheckType.GYM, isDone = true),
                DailyCheck(FRIDAY, DailyCheckType.DIET, isDone = false),
                DailyCheck(THURSDAY, DailyCheckType.ENGLISH, isDone = true),
            ),
        )

        val recorded = ObserveDailyChecksUseCase(repository)(FRIDAY).first()

        assertEquals(
            listOf(
                DailyCheck(FRIDAY, DailyCheckType.GYM, isDone = true),
                DailyCheck(FRIDAY, DailyCheckType.DIET, isDone = false),
            ),
            recorded,
        )
    }
}
