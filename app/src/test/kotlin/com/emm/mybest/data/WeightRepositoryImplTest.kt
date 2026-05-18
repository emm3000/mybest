package com.emm.mybest.data

import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightRepositoryImplTest {

    private val dao = mockk<DailyWeightDao>()
    private val repository = WeightRepositoryImpl(dao)

    @Test
    fun `getWeightProgress maps dao entities to domain models`() = runTest {
        val entity = DailyWeightEntity(
            id = "w1",
            date = LocalDate(2026, 3, 8),
            weight = 71.3f,
            note = "ok",
        )
        every { dao.observeAllOrdered() } returns flowOf(listOf(entity))

        val result = repository.getWeightProgress().first()

        assertEquals(1, result.size)
        assertEquals("w1", result.first().id)
        assertEquals(71.3f, result.first().weight)
        assertEquals("ok", result.first().note)
    }

    @Test
    fun `saveWeight delegates to dao upsert`() = runTest {
        val saved = slot<DailyWeightEntity>()
        coEvery { dao.upsert(capture(saved)) } returns Unit

        repository.saveWeight(weight = 80.2f, note = "after lunch")

        coVerify(exactly = 1) { dao.upsert(any()) }
        assertEquals(80.2f, saved.captured.weight)
        assertEquals("after lunch", saved.captured.note)
    }

    @Test
    fun `deleteByDate delegates to dao`() = runTest {
        val date = LocalDate(2026, 3, 7)
        coEvery { dao.deleteByDate(date) } returns Unit

        repository.deleteByDate(date)

        coVerify(exactly = 1) { dao.deleteByDate(date) }
    }
}
