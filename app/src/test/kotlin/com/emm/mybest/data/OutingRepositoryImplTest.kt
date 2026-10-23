package com.emm.mybest.data

import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.NoOutingAnswerDao
import com.emm.mybest.data.entities.OutingDao
import com.emm.mybest.data.entities.OutingEntity
import com.emm.mybest.data.entities.WonNightDao
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.OutingKind
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)
private val LIMITS = Limits(money = Money(12_000), drinks = 4, returnTime = LocalTime(2, 0))
private const val TAKEN_APP_DAY = -1L

class OutingRepositoryImplTest {

    private val outingDao = mockk<OutingDao>()
    private val wonNightDao = mockk<WonNightDao>()
    private val noOutingAnswerDao = mockk<NoOutingAnswerDao>()
    private val repository = OutingRepositoryImpl(outingDao, wonNightDao, noOutingAnswerDao)

    @Test
    fun `recordPlanned returns the new outing id`() = runTest {
        coEvery { outingDao.insert(any()) } returns 9

        assertEquals(9L, repository.recordPlanned(FRIDAY, LIMITS))
    }

    @Test
    fun `a second planned outing on a taken app day fails`() = runTest {
        coEvery { outingDao.insert(any()) } returns TAKEN_APP_DAY

        val failure = runCatching { repository.recordPlanned(FRIDAY, LIMITS) }.exceptionOrNull()

        assertTrue(failure is IllegalStateException)
    }

    @Test
    fun `a second unplanned outing on a taken app day fails`() = runTest {
        coEvery { outingDao.insert(any()) } returns TAKEN_APP_DAY

        val failure = runCatching { repository.recordUnplanned(FRIDAY) }.exceptionOrNull()

        assertTrue(failure is IllegalStateException)
    }

    @Test
    fun `saveCheckIn writes the check-in columns on the stored outing`() = runTest {
        val stored = OutingEntity(id = 4, appDay = FRIDAY, kind = OutingKind.UNPLANNED)
        val updated = slot<OutingEntity>()
        coEvery { outingDao.findById(4) } returns stored
        coEvery { outingDao.update(capture(updated)) } returns Unit

        repository.saveCheckIn(
            4,
            CheckIn(totalSpent = Money(8_000), drinks = 6, returnTime = LocalTime(3, 0), mood = 1),
        )

        assertEquals(
            stored.copy(spentCents = 8_000, drinks = 6, returnMinutes = 180, mood = 1),
            updated.captured,
        )
    }

    @Test
    fun `observeNoOutingAnswers emits the answered app days as a set`() = runTest {
        every { noOutingAnswerDao.observeRange(FRIDAY, SATURDAY) } returns flowOf(listOf(FRIDAY, SATURDAY))

        assertEquals(setOf(FRIDAY, SATURDAY), repository.observeNoOutingAnswers(FRIDAY, SATURDAY).first())
    }
}
