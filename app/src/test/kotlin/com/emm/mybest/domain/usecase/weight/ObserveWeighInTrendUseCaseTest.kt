package com.emm.mybest.domain.usecase.weight

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WeighInDirection
import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.testing.FakeSetupRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveWeighInTrendUseCaseTest {

    private suspend fun trendOf(weights: List<Float>, targetKg: Float? = null): WeighInTrend {
        val entries = weights.mapIndexed { index, kg ->
            WeightEntry(id = "w$index", date = LocalDate(2026, 10, 1 + index), weight = kg)
        }
        val weightRepository = mockk<WeightRepository>()
        every { weightRepository.getWeightProgress() } returns flowOf(entries)
        val setupRepository = FakeSetupRepository(Setup(targetWeightKg = targetKg))
        val useCase = ObserveWeighInTrendUseCase(weightRepository, setupRepository)
        return useCase().first()
    }

    @Test
    fun `no weigh-ins gives all null`() = runTest {
        assertEquals(WeighInTrend(), trendOf(emptyList(), targetKg = 70f))
    }

    @Test
    fun `one weigh-in has no change`() = runTest {
        val trend = trendOf(listOf(80f))

        assertEquals(80f, trend.latestKg)
        assertEquals(null, trend.changeSincePreviousKg)
    }

    @Test
    fun `two weigh-ins give latest minus previous`() = runTest {
        val trend = trendOf(listOf(81f, 80.5f))

        assertEquals(80.5f, trend.latestKg)
        assertEquals(-0.5f, trend.changeSincePreviousKg)
    }

    @Test
    fun `unset target gives null remaining`() = runTest {
        assertEquals(null, trendOf(listOf(80f)).remainingToTargetKg)
    }

    @Test
    fun `remaining is latest minus target`() = runTest {
        assertEquals(4f, trendOf(listOf(82f, 80f), targetKg = 76f).remainingToTargetKg)
    }

    @Test
    fun `target without weigh-ins gives null remaining`() = runTest {
        assertEquals(null, trendOf(emptyList(), targetKg = 76f).remainingToTargetKg)
    }

    @Test
    fun `remaining is null once the latest weigh-in reaches the target`() = runTest {
        assertEquals(null, trendOf(listOf(78f), targetKg = 78f).remainingToTargetKg)
    }

    @Test
    fun `remaining is null when the latest weigh-in is below the target`() = runTest {
        assertEquals(null, trendOf(listOf(77f), targetKg = 78f).remainingToTargetKg)
    }

    @Test
    fun `direction is down when the weight dropped`() = runTest {
        assertEquals(WeighInDirection.DOWN, trendOf(listOf(81f, 80.5f)).direction)
    }

    @Test
    fun `direction is up when the weight rose`() = runTest {
        assertEquals(WeighInDirection.UP, trendOf(listOf(80f, 80.5f)).direction)
    }

    @Test
    fun `direction is unchanged when the weight did not move`() = runTest {
        assertEquals(WeighInDirection.UNCHANGED, trendOf(listOf(80f, 80f)).direction)
    }

    @Test
    fun `direction is null without a previous weigh-in`() = runTest {
        assertEquals(null, trendOf(listOf(80f)).direction)
    }
}
