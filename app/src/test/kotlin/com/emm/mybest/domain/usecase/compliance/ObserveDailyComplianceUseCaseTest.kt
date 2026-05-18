package com.emm.mybest.domain.usecase.compliance

import com.emm.mybest.domain.models.ExerciseCompliance
import com.emm.mybest.domain.models.MealCompliance
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.repository.ComplianceRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveDailyComplianceUseCaseTest {

    private val repo = mockk<ComplianceRepository>()
    private val useCase = ObserveDailyComplianceUseCase(repo)
    private val date = LocalDate(2026, 5, 18)

    @Test
    fun `invoke emits all false when no meals and no exercise recorded`() = runTest {
        every { repo.observeMealsByDate(date) } returns flowOf(emptyList())
        every { repo.observeExerciseByDate(date) } returns flowOf(null)

        val result = useCase(date).first()

        assertEquals(date, result.date)
        assertEquals(MealType.entries.size, result.mealsDone.size)
        result.mealsDone.values.forEach { assertFalse(it) }
        assertFalse(result.exerciseDone)
        assertEquals(0f, result.completionRatio)
    }

    @Test
    fun `invoke emits correct map when two meals and exercise are done`() = runTest {
        val meals = listOf(
            MealCompliance(date, MealType.BREAKFAST, done = true),
            MealCompliance(date, MealType.LUNCH, done = true),
        )
        val exercise = ExerciseCompliance(date, done = true)
        every { repo.observeMealsByDate(date) } returns flowOf(meals)
        every { repo.observeExerciseByDate(date) } returns flowOf(exercise)

        val result = useCase(date).first()

        assertTrue(result.mealsDone[MealType.BREAKFAST] == true)
        assertTrue(result.mealsDone[MealType.LUNCH] == true)
        assertFalse(result.mealsDone[MealType.DINNER] == true)
        assertFalse(result.mealsDone[MealType.SNACK] == true)
        assertTrue(result.exerciseDone)
        // 3 done out of 5 total (4 meals + 1 exercise)
        assertEquals(3f / 5f, result.completionRatio)
    }
}
