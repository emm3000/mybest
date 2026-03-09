package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.DailyHabitSummary
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.DailyHabitRepository
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GetInsightsUseCaseTest {

    private val weightRepository = mockk<WeightRepository>()
    private val dailyHabitRepository = mockk<DailyHabitRepository>()
    private val photoRepository = mockk<PhotoRepository>()
    private val useCase = GetInsightsUseCase(weightRepository, dailyHabitRepository, photoRepository)

    @Test
    fun `invoke calculates consistency and weight metrics`() = runTest {
        val weights = listOf(
            WeightEntry(id = "w-1", date = LocalDate(2026, 3, 1), weight = 83f),
            WeightEntry(id = "w-2", date = LocalDate(2026, 3, 8), weight = 80f),
        )
        val habits = listOf(
            DailyHabitSummary(
                date = LocalDate(2026, 3, 1),
                ateHealthy = true,
                didExercise = true,
                notes = null,
            ),
            DailyHabitSummary(
                date = LocalDate(2026, 3, 2),
                ateHealthy = false,
                didExercise = true,
                notes = null,
            ),
        )
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { dailyHabitRepository.getAllDailyHabits() } returns flowOf(habits)
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val insights = useCase().single()

        assertEquals(83f, insights.initialWeight)
        assertEquals(80f, insights.currentWeight)
        assertEquals(3f, insights.totalWeightLost)
        assertEquals(2, insights.exerciseDays)
        assertEquals(1, insights.healthyEatingDays)
        assertEquals(0.75f, insights.habitConsistency)
        assertEquals(0, insights.photoCount)
    }

    @Test
    fun `invoke returns zero consistency when there are no habits`() = runTest {
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { dailyHabitRepository.getAllDailyHabits() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val insights = useCase().single()

        assertEquals(0f, insights.habitConsistency)
        assertEquals(0f, insights.currentWeight)
        assertEquals(0f, insights.initialWeight)
        assertEquals(0f, insights.totalWeightLost)
        assertEquals(0, insights.exerciseDays)
        assertEquals(0, insights.healthyEatingDays)
        assertEquals(0, insights.photoCount)
    }
}
