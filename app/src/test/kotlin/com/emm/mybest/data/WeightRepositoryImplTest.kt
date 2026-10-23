package com.emm.mybest.data

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.testing.FixedClock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val SATURDAY_TWO_AM = LocalDateTime(2026, 10, 24, 2, 0).toInstant(LIMA)

class WeightRepositoryImplTest {

    private val dao = mockk<DailyWeightDao>()
    private val repository = WeightRepositoryImpl(dao, AppDayClock(FixedClock(SATURDAY_TWO_AM), LIMA))

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
    fun `a weigh-in saved at 02_00 is stored under the previous app day`() = runTest {
        val saved = slot<DailyWeightEntity>()
        coEvery { dao.upsert(capture(saved)) } returns Unit

        repository.saveWeight(weight = 80.2f, note = null)

        assertEquals(LocalDate(2026, 10, 23), saved.captured.date)
    }

    @Test
    fun `deleteByDate delegates to dao`() = runTest {
        val date = LocalDate(2026, 3, 7)
        coEvery { dao.deleteByDate(date) } returns Unit

        repository.deleteByDate(date)

        coVerify(exactly = 1) { dao.deleteByDate(date) }
    }
}
