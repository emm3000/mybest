package com.emm.mybest.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.emm.mybest.domain.reminder.ReminderDelays
import com.emm.mybest.testing.FixedClock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "daily_test_reminder"
private val LIMA = TimeZone.of("America/Lima")
private val MADRID = TimeZone.of("Europe/Madrid")
private val NINE_IN_THE_MORNING = LocalDateTime(2026, 10, 23, 9, 0).toInstant(LIMA)
private val REMINDER_TIME = LocalTime(10, 0)
private val LATER_REMINDER_TIME = LocalTime(11, 30)
private const val HOURS_TO_REMINDER = 1L
private const val MINUTES_TO_LATER_REMINDER = 150L

private class DailyTestWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = Result.success()
}

class DailyReminderWorkTest {

    private val workManager = mockk<WorkManager>(relaxed = true)
    private val names = mutableListOf<String>()
    private val policies = mutableListOf<ExistingPeriodicWorkPolicy>()
    private val requests = mutableListOf<PeriodicWorkRequest>()
    private val existingWork = mutableListOf<WorkInfo>()
    private val reminderDelays = ReminderDelays(FixedClock(NINE_IN_THE_MORNING), LIMA)

    init {
        every {
            workManager.enqueueUniquePeriodicWork(capture(names), capture(policies), capture(requests))
        } returns mockk(relaxed = true)
        every { workManager.getWorkInfosForUniqueWorkFlow(WORK_NAME) } answers {
            flowOf(existingWork.toList())
        }
    }

    private suspend fun schedule(time: LocalTime) =
        workManager.scheduleDailyReminder<DailyTestWorker>(WORK_NAME, time, reminderDelays)

    private fun workAt(time: LocalTime, state: WorkInfo.State, zone: TimeZone = LIMA) =
        WorkInfo(UUID.randomUUID(), state, setOf(reminderTimeTag(time, zone)))

    @Test
    fun `schedules one unique daily work with the given worker`() = runTest {
        schedule(REMINDER_TIME)

        val spec = requests.single().workSpec
        assertEquals(listOf(WORK_NAME), names)
        assertEquals(TimeUnit.DAYS.toMillis(1), spec.intervalDuration)
        assertEquals(DailyTestWorker::class.java.name, spec.workerClassName)
    }

    @Test
    fun `delays the work to the next occurrence of the time`() = runTest {
        schedule(REMINDER_TIME)

        assertEquals(HOURS_TO_REMINDER, TimeUnit.MILLISECONDS.toHours(requests.single().workSpec.initialDelay))
    }

    @Test
    fun `cancels and re-enqueues so a changed time takes effect`() = runTest {
        schedule(REMINDER_TIME)
        schedule(LATER_REMINDER_TIME)

        assertEquals(List(2) { ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE }, policies)
        assertEquals(MINUTES_TO_LATER_REMINDER, TimeUnit.MILLISECONDS.toMinutes(requests.last().workSpec.initialDelay))
    }

    @Test
    fun `tags the work with its time`() = runTest {
        schedule(REMINDER_TIME)

        assertTrue(reminderTimeTag(REMINDER_TIME, LIMA) in requests.single().tags)
    }

    @Test
    fun `does not enqueue when already scheduled at the same time`() = runTest {
        existingWork += workAt(REMINDER_TIME, WorkInfo.State.ENQUEUED)

        schedule(REMINDER_TIME)

        assertTrue(names.isEmpty())
    }

    @Test
    fun `does not enqueue when the work is running at the same time`() = runTest {
        existingWork += workAt(REMINDER_TIME, WorkInfo.State.RUNNING)

        schedule(REMINDER_TIME)

        assertTrue(names.isEmpty())
    }

    @Test
    fun `re-enqueues work scheduled at the same time in another time zone`() = runTest {
        existingWork += workAt(REMINDER_TIME, WorkInfo.State.ENQUEUED, MADRID)

        schedule(REMINDER_TIME)

        assertEquals(listOf(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE), policies)
    }

    @Test
    fun `re-enqueues work scheduled at a different time`() = runTest {
        existingWork += workAt(REMINDER_TIME, WorkInfo.State.ENQUEUED)

        schedule(LATER_REMINDER_TIME)

        assertEquals(listOf(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE), policies)
    }

    @Test
    fun `enqueues again when the work with the same time is finished`() = runTest {
        existingWork += workAt(REMINDER_TIME, WorkInfo.State.SUCCEEDED)

        schedule(REMINDER_TIME)

        assertEquals(listOf(WORK_NAME), names)
    }
}
