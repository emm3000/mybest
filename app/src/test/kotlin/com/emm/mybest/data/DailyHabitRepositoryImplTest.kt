package com.emm.mybest.data

import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyHabitEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyHabitRepositoryImplTest {

    private val dao = mockk<DailyHabitDao>()
    private val repository = DailyHabitRepositoryImpl(dao)

    @Test
    fun `getAllDailyHabits maps dao entities to domain models`() = runTest {
        val entity = DailyHabitEntity(
            date = LocalDate(2026, 3, 8),
            ateHealthy = true,
            didExercise = false,
            notes = "leg day",
        )
        every { dao.observeAll() } returns flowOf(listOf(entity))

        val result = repository.getAllDailyHabits().first()

        assertEquals(1, result.size)
        assertEquals(LocalDate(2026, 3, 8), result.first().date)
        assertTrue(result.first().ateHealthy)
        assertEquals("leg day", result.first().notes)
    }

    @Test
    fun `deleteByDate delegates to dao`() = runTest {
        val date = LocalDate(2026, 3, 6)
        coEvery { dao.deleteByDate(date) } returns Unit

        repository.deleteByDate(date)

        coVerify(exactly = 1) { dao.deleteByDate(date) }
    }
}
