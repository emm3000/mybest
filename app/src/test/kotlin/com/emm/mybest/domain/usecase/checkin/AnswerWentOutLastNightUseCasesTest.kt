package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.testing.FakeOutingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)
private val WON_NIGHT = WonNight(FRIDAY, Money(380_00))
private val PLANNED = Outing(1, FRIDAY, OutingKind.PLANNED, Limits(Money(200_00), 4, LocalTime(2, 0)), checkIn = null)

class AnswerWentOutLastNightUseCasesTest {

    @Test
    fun `yes records an unplanned outing pending its check-in`() = runTest {
        val repository = FakeOutingRepository()

        val result = RecordUnplannedOutingUseCase(repository)(FRIDAY)

        assertEquals(RecordUnplannedOutingResult.Recorded, result)
        val outing = repository.observeAll().first().single()
        assertEquals(OutingKind.UNPLANNED, outing.kind)
        assertEquals(FRIDAY, outing.appDay)
        assertTrue(outing.isPendingCheckIn)
    }

    @Test
    fun `yes deletes the won night of that evening`() = runTest {
        val repository = FakeOutingRepository(initialWonNights = listOf(WON_NIGHT))

        RecordUnplannedOutingUseCase(repository)(FRIDAY)

        assertTrue(repository.observeWonNights(FRIDAY, FRIDAY).first().isEmpty())
    }

    @Test
    fun `yes changes nothing when the evening already has an outing`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(PLANNED), initialWonNights = listOf(WON_NIGHT))

        val result = RecordUnplannedOutingUseCase(repository)(FRIDAY)

        assertEquals(RecordUnplannedOutingResult.AlreadyRecorded, result)
        assertEquals(listOf(PLANNED), repository.observeAll().first())
        assertEquals(listOf(WON_NIGHT), repository.observeWonNights(FRIDAY, FRIDAY).first())
    }

    @Test
    fun `no records the answer and the question does not come back`() = runTest {
        val repository = FakeOutingRepository()

        RecordNoOutingUseCase(repository)(FRIDAY)

        assertEquals(setOf(FRIDAY), repository.observeNoOutingAnswers(FRIDAY, FRIDAY).first())
        assertTrue(repository.observeAll().first().isEmpty())
    }
}
