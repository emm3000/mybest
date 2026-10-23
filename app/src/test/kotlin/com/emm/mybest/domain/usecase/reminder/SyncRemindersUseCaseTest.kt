package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.reminder.CheckInReminderScheduler
import com.emm.mybest.domain.reminder.PreOutingReminderScheduler
import com.emm.mybest.domain.reminder.ReminderScheduler
import com.emm.mybest.domain.reminder.ReminderSchedulers
import com.emm.mybest.domain.reminder.WeekendReminderSchedulers
import com.emm.mybest.domain.reminder.WeightReminderScheduler
import com.emm.mybest.testing.FakeSetupRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

private val EVENING = LocalTime(19, 0)
private val LATE_EVENING = LocalTime(21, 0)
private val MORNING = LocalTime(10, 0)

private abstract class RecordingScheduler : ReminderScheduler {
    val calls = mutableListOf<LocalTime?>()

    override suspend fun schedule(time: LocalTime) {
        calls += time
    }

    override suspend fun cancel() {
        calls += null
    }
}

private class RecordingPreOutingScheduler : RecordingScheduler(), PreOutingReminderScheduler

private class RecordingCheckInScheduler : RecordingScheduler(), CheckInReminderScheduler

private class RecordingWeightScheduler : WeightReminderScheduler {
    val calls = mutableListOf<Pair<DayOfWeek, LocalTime>?>()

    override suspend fun schedule(day: DayOfWeek, time: LocalTime) {
        calls += day to time
    }

    override suspend fun cancel() {
        calls += null
    }
}

private val WEIGH_IN = Setup(weighInDay = DayOfWeek.FRIDAY, weighInReminderTime = MORNING)

class SyncRemindersUseCaseTest {

    private val repository = FakeSetupRepository()
    private val preOuting = RecordingPreOutingScheduler()
    private val checkIn = RecordingCheckInScheduler()
    private val weighIn = RecordingWeightScheduler()

    private fun TestScope.startSync() {
        backgroundScope.launch {
            SyncRemindersUseCase(
                repository,
                ReminderSchedulers(WeekendReminderSchedulers(preOuting, checkIn), weighIn),
            )()
        }
        runCurrent()
    }

    private suspend fun TestScope.save(setup: Setup) {
        repository.save(setup)
        runCurrent()
    }

    @Test
    fun `schedules the pre-outing reminder when its time is set`() = runTest {
        startSync()

        save(Setup(preOutingReminderTime = EVENING))

        assertEquals(listOf<LocalTime?>(null, EVENING), preOuting.calls)
    }

    @Test
    fun `cancels the pre-outing reminder when its time is cleared`() = runTest {
        startSync()
        save(Setup(preOutingReminderTime = EVENING))

        save(Setup(preOutingReminderTime = null))

        assertEquals(listOf<LocalTime?>(null, EVENING, null), preOuting.calls)
    }

    @Test
    fun `schedules again with the new time when the time changes`() = runTest {
        startSync()
        save(Setup(preOutingReminderTime = EVENING))

        save(Setup(preOutingReminderTime = LATE_EVENING))

        assertEquals(listOf<LocalTime?>(null, EVENING, LATE_EVENING), preOuting.calls)
    }

    @Test
    fun `schedules the check-in reminder when its time is set`() = runTest {
        startSync()

        save(Setup(checkInReminderTime = MORNING))

        assertEquals(listOf<LocalTime?>(null, MORNING), checkIn.calls)
    }

    @Test
    fun `does not reschedule on an unrelated setup change`() = runTest {
        startSync()
        save(Setup(preOutingReminderTime = EVENING, checkInReminderTime = MORNING))

        save(Setup(preOutingReminderTime = EVENING, checkInReminderTime = MORNING, monthlyOutingLimit = 4))

        assertEquals(listOf<LocalTime?>(null, EVENING), preOuting.calls)
        assertEquals(listOf<LocalTime?>(null, MORNING), checkIn.calls)
    }

    @Test
    fun `changing the pre-outing time leaves the check-in reminder alone`() = runTest {
        startSync()
        save(Setup(preOutingReminderTime = EVENING, checkInReminderTime = MORNING))

        save(Setup(preOutingReminderTime = LATE_EVENING, checkInReminderTime = MORNING))

        assertEquals(listOf<LocalTime?>(null, MORNING), checkIn.calls)
    }

    @Test
    fun `schedules the weigh-in reminder on its day and time`() = runTest {
        startSync()

        save(WEIGH_IN)

        assertEquals(listOf(null, DayOfWeek.FRIDAY to MORNING), weighIn.calls)
    }

    @Test
    fun `cancels the weigh-in reminder when its day is cleared`() = runTest {
        startSync()
        save(WEIGH_IN)

        save(WEIGH_IN.copy(weighInDay = null))

        assertEquals(listOf(null, DayOfWeek.FRIDAY to MORNING, null), weighIn.calls)
    }

    @Test
    fun `cancels the weigh-in reminder when its time is cleared`() = runTest {
        startSync()
        save(WEIGH_IN)

        save(WEIGH_IN.copy(weighInReminderTime = null))

        assertEquals(listOf(null, DayOfWeek.FRIDAY to MORNING, null), weighIn.calls)
    }

    @Test
    fun `schedules the weigh-in reminder again when its day changes`() = runTest {
        startSync()
        save(WEIGH_IN)

        save(WEIGH_IN.copy(weighInDay = DayOfWeek.SUNDAY))

        assertEquals(listOf(null, DayOfWeek.FRIDAY to MORNING, DayOfWeek.SUNDAY to MORNING), weighIn.calls)
    }

    @Test
    fun `turning the photo reminder on leaves the weigh-in schedule alone`() = runTest {
        startSync()
        save(WEIGH_IN)

        save(WEIGH_IN.copy(isPhotoReminderEnabled = true))

        assertEquals(listOf(null, DayOfWeek.FRIDAY to MORNING), weighIn.calls)
    }
}
