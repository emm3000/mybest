package com.emm.mybest.data

import com.emm.mybest.data.entities.ExerciseComplianceDao
import com.emm.mybest.data.entities.ExerciseComplianceEntity
import com.emm.mybest.data.entities.MealComplianceDao
import com.emm.mybest.data.entities.MealComplianceEntity
import com.emm.mybest.domain.models.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ComplianceRepositoryImplTest {

    private val mealDao = FakeMealComplianceDao()
    private val exerciseDao = FakeExerciseComplianceDao()
    private val repository = ComplianceRepositoryImpl(mealDao, exerciseDao)

    @Test
    fun `observeMealsByDate emits domain meals for matching date only`() = runTest {
        val date = LocalDate(2026, 5, 22)
        val other = LocalDate(2026, 5, 21)
        mealDao.state.value = listOf(
            MealComplianceEntity(date, MealType.BREAKFAST.name, true),
            MealComplianceEntity(other, MealType.LUNCH.name, false),
        )

        val result = repository.observeMealsByDate(date).first()

        assertEquals(1, result.size)
        assertEquals(MealType.BREAKFAST, result.first().mealType)
        assertEquals(true, result.first().done)
    }

    @Test
    fun `observeMealsByDate emits empty list when nothing stored for date`() = runTest {
        val date = LocalDate(2026, 5, 22)
        mealDao.state.value = emptyList()

        val result = repository.observeMealsByDate(date).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `observeExerciseByDate emits domain exercise for matching date`() = runTest {
        val date = LocalDate(2026, 5, 22)
        exerciseDao.state.value = listOf(ExerciseComplianceEntity(date, true))

        val result = repository.observeExerciseByDate(date).first()

        assertEquals(date, result?.date)
        assertEquals(true, result?.done)
    }

    @Test
    fun `observeExerciseByDate emits null when no record`() = runTest {
        val date = LocalDate(2026, 5, 22)
        exerciseDao.state.value = emptyList()

        val result = repository.observeExerciseByDate(date).first()

        assertNull(result)
    }

    @Test
    fun `observeMealsRange filters inclusive from to`() = runTest {
        val from = LocalDate(2026, 5, 20)
        val to = LocalDate(2026, 5, 22)
        mealDao.state.value = listOf(
            MealComplianceEntity(LocalDate(2026, 5, 19), MealType.LUNCH.name, false),
            MealComplianceEntity(LocalDate(2026, 5, 20), MealType.BREAKFAST.name, true),
            MealComplianceEntity(LocalDate(2026, 5, 22), MealType.DINNER.name, true),
            MealComplianceEntity(LocalDate(2026, 5, 23), MealType.SNACK.name, false),
        )

        val result = repository.observeMealsRange(from, to).first()

        assertEquals(2, result.size)
        assertEquals(LocalDate(2026, 5, 20), result[0].date)
        assertEquals(LocalDate(2026, 5, 22), result[1].date)
    }

    @Test
    fun `observeExerciseRange filters inclusive from to and skips null mappings`() = runTest {
        val from = LocalDate(2026, 5, 20)
        val to = LocalDate(2026, 5, 22)
        exerciseDao.state.value = listOf(
            ExerciseComplianceEntity(LocalDate(2026, 5, 19), false),
            ExerciseComplianceEntity(LocalDate(2026, 5, 20), true),
            ExerciseComplianceEntity(LocalDate(2026, 5, 22), false),
            ExerciseComplianceEntity(LocalDate(2026, 5, 23), true),
        )

        val result = repository.observeExerciseRange(from, to).first()

        assertEquals(2, result.size)
        assertEquals(LocalDate(2026, 5, 20), result[0].date)
        assertEquals(LocalDate(2026, 5, 22), result[1].date)
    }

    @Test
    fun `toggleMeal upserts entity with mapped fields`() = runTest {
        val date = LocalDate(2026, 5, 22)

        repository.toggleMeal(date, MealType.LUNCH, done = true)

        val upserted = mealDao.state.value.single()
        assertEquals(date, upserted.date)
        assertEquals(MealType.LUNCH.name, upserted.mealType)
        assertEquals(true, upserted.done)
    }

    @Test
    fun `toggleExercise upserts entity with mapped fields`() = runTest {
        val date = LocalDate(2026, 5, 22)

        repository.toggleExercise(date, done = false)

        val upserted = exerciseDao.state.value.single()
        assertEquals(date, upserted.date)
        assertEquals(false, upserted.done)
    }
}

private class FakeMealComplianceDao : MealComplianceDao {

    val state = MutableStateFlow<List<MealComplianceEntity>>(emptyList())

    override fun observeByDate(date: LocalDate): Flow<List<MealComplianceEntity>> =
        state.map { list -> list.filter { it.date == date } }

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<MealComplianceEntity>> =
        state.map { list -> list.filter { it.date in from..to } }

    override suspend fun upsert(entry: MealComplianceEntity) {
        val current = state.value.toMutableList()
        val index = current.indexOfFirst { it.date == entry.date && it.mealType == entry.mealType }
        if (index >= 0) current[index] = entry else current.add(entry)
        state.value = current
    }
}

private class FakeExerciseComplianceDao : ExerciseComplianceDao {

    val state = MutableStateFlow<List<ExerciseComplianceEntity>>(emptyList())

    override fun observeByDate(date: LocalDate): Flow<ExerciseComplianceEntity?> =
        state.map { list -> list.firstOrNull { it.date == date } }

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<ExerciseComplianceEntity>> =
        state.map { list -> list.filter { it.date in from..to } }

    override suspend fun upsert(entry: ExerciseComplianceEntity) {
        val current = state.value.toMutableList()
        val index = current.indexOfFirst { it.date == entry.date }
        if (index >= 0) current[index] = entry else current.add(entry)
        state.value = current
    }
}
