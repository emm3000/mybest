package com.emm.mybest.domain.usecase.compliance

import com.emm.mybest.domain.models.ExerciseCompliance
import com.emm.mybest.domain.models.MealCompliance
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.repository.ComplianceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCompletionStreakUseCaseTest {

    private val repo = mockk<ComplianceRepository>()
    private val useCase = GetCompletionStreakUseCase(repo)
    private val today = LocalDate(2026, 5, 21)

    private fun allMeals(date: LocalDate): List<MealCompliance> =
        MealType.entries.map { MealCompliance(date, it, done = true) }

    private fun exercise(date: LocalDate, done: Boolean = true) = ExerciseCompliance(date, done)

    private fun stubRepo(meals: List<MealCompliance>, exercises: List<ExerciseCompliance>) {
        val from = today.minus(DatePeriod(days = 365))
        every { repo.observeMealsRange(from, today) } returns flowOf(meals)
        every { repo.observeExerciseRange(from, today) } returns flowOf(exercises)
    }

    @Test
    fun `empty meals and exercises yields streak zero`() = runTest {
        stubRepo(emptyList(), emptyList())
        assertEquals(0, useCase(today).first())
    }

    @Test
    fun `today and yesterday complete day before incomplete yields streak two`() = runTest {
        val yesterday = today.minus(DatePeriod(days = 1))
        val dayBefore = today.minus(DatePeriod(days = 2))
        stubRepo(
            meals = allMeals(today) + allMeals(yesterday),
            exercises = listOf(exercise(today), exercise(yesterday), exercise(dayBefore)),
        )
        assertEquals(2, useCase(today).first())
    }

    @Test
    fun `today and four prior days complete yields streak five`() = runTest {
        val days = (0..4).map { today.minus(DatePeriod(days = it)) }
        stubRepo(
            meals = days.flatMap { allMeals(it) },
            exercises = days.map { exercise(it) },
        )
        assertEquals(5, useCase(today).first())
    }

    @Test
    fun `today incomplete yesterday and day before complete yields streak two`() = runTest {
        val yesterday = today.minus(DatePeriod(days = 1))
        val dayBefore = today.minus(DatePeriod(days = 2))
        stubRepo(
            meals = allMeals(yesterday) + allMeals(dayBefore),
            exercises = listOf(exercise(yesterday), exercise(dayBefore)),
        )
        assertEquals(2, useCase(today).first())
    }

    @Test
    fun `today incomplete yesterday incomplete yields streak zero`() = runTest {
        stubRepo(emptyList(), emptyList())
        assertEquals(0, useCase(today).first())
    }

    @Test
    fun `today all meals done but exercise not done yields streak zero`() = runTest {
        stubRepo(
            meals = allMeals(today),
            exercises = listOf(exercise(today, done = false)),
        )
        assertEquals(0, useCase(today).first())
    }

    @Test
    fun `today exercise done but only three meals done yields streak zero`() = runTest {
        val threeMeals = MealType.entries.take(3).map { MealCompliance(today, it, done = true) }
        stubRepo(
            meals = threeMeals,
            exercises = listOf(exercise(today)),
        )
        assertEquals(0, useCase(today).first())
    }

    @Test
    fun `today three meals and exercise done still incomplete yields streak zero`() = runTest {
        val threeMeals = MealType.entries.take(3).map { MealCompliance(today, it, done = true) }
        stubRepo(
            meals = threeMeals,
            exercises = listOf(exercise(today)),
        )
        assertEquals(0, useCase(today).first())
    }

    @Test
    fun `lookback range starts at today minus 365 days`() = runTest {
        val from = today.minus(DatePeriod(days = 365))
        stubRepo(emptyList(), emptyList())
        useCase(today).first()
        verify { repo.observeMealsRange(from, today) }
    }
}
