package com.emm.mybest.data.reminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.emm.mybest.domain.reminder.ReminderDelays
import com.emm.mybest.testing.FixedClock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

private val LIMA = TimeZone.of("America/Lima")
private val MADRID = TimeZone.of("Europe/Madrid")
private val THURSDAY_EVENING = LocalDateTime(2026, 10, 22, 20, 0).toInstant(LIMA)
private val REMINDER_TIME = LocalTime(19, 0)
private val LATER_REMINDER_TIME = LocalTime(21, 0)
private const val HOURS_TO_FRIDAY_REMINDER = 23L
private const val HOURS_TO_SATURDAY_REMINDER = 47L
private const val HOURS_TO_LATER_FRIDAY_REMINDER = 25L

class PreOutingReminderSchedulerImplTest {

    private val workManager = mockk<WorkManager>(relaxed = true)
    private val names = mutableListOf<String>()
    private val policies = mutableListOf<ExistingPeriodicWorkPolicy>()
    private val requests = mutableListOf<PeriodicWorkRequest>()
    private val existingWork = mutableMapOf<String, List<WorkInfo>>()
    private val scheduler = PreOutingReminderSchedulerImpl(
        workManager,
        ReminderDelays(FixedClock(THURSDAY_EVENING), LIMA),
    )

    init {
        every {
            workManager.enqueueUniquePeriodicWork(capture(names), capture(policies), capture(requests))
        } returns mockk(relaxed = true)
        every { workManager.getWorkInfosForUniqueWorkFlow(any()) } answers {
            flowOf(existingWork[firstArg()].orEmpty())
        }
    }

    private fun workAt(time: LocalTime, state: WorkInfo.State, zone: TimeZone = LIMA) =
        listOf(WorkInfo(UUID.randomUUID(), state, setOf(reminderTimeTag(time, zone))))

    private fun requestNamed(name: String): PeriodicWorkRequest = requests[names.lastIndexOf(name)]

    private fun delayHours(name: String): Long =
        TimeUnit.MILLISECONDS.toHours(requestNamed(name).workSpec.initialDelay)

    @Test
    fun `schedules one unique work for friday and one for saturday`() = runTest {
        scheduler.schedule(REMINDER_TIME)

        assertEquals(listOf(PRE_OUTING_FRIDAY_WORK_NAME, PRE_OUTING_SATURDAY_WORK_NAME), names)
    }

    @Test
    fun `delays each work to the next occurrence of its day`() = runTest {
        scheduler.schedule(REMINDER_TIME)

        assertEquals(HOURS_TO_FRIDAY_REMINDER, delayHours(PRE_OUTING_FRIDAY_WORK_NAME))
        assertEquals(HOURS_TO_SATURDAY_REMINDER, delayHours(PRE_OUTING_SATURDAY_WORK_NAME))
    }

    @Test
    fun `repeats every seven days with the pre-outing worker`() = runTest {
        scheduler.schedule(REMINDER_TIME)

        requests.forEach { request ->
            assertEquals(TimeUnit.DAYS.toMillis(7), request.workSpec.intervalDuration)
            assertEquals(PreOutingReminderWorker::class.java.name, request.workSpec.workerClassName)
        }
    }

    @Test
    fun `cancels and re-enqueues so a changed time takes effect`() = runTest {
        scheduler.schedule(REMINDER_TIME)
        scheduler.schedule(LATER_REMINDER_TIME)

        assertEquals(List(4) { ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE }, policies)
        assertEquals(HOURS_TO_LATER_FRIDAY_REMINDER, delayHours(PRE_OUTING_FRIDAY_WORK_NAME))
    }

    @Test
    fun `tags every work with its time`() = runTest {
        scheduler.schedule(REMINDER_TIME)

        requests.forEach { request -> assertTrue(reminderTimeTag(REMINDER_TIME, LIMA) in request.tags) }
    }

    @Test
    fun `does not enqueue a day that is already scheduled at the same time`() = runTest {
        existingWork[PRE_OUTING_FRIDAY_WORK_NAME] = workAt(REMINDER_TIME, WorkInfo.State.ENQUEUED)

        scheduler.schedule(REMINDER_TIME)

        assertEquals(listOf(PRE_OUTING_SATURDAY_WORK_NAME), names)
    }

    @Test
    fun `does not enqueue a day whose work is running at the same time`() = runTest {
        existingWork[PRE_OUTING_FRIDAY_WORK_NAME] = workAt(REMINDER_TIME, WorkInfo.State.RUNNING)

        scheduler.schedule(REMINDER_TIME)

        assertEquals(listOf(PRE_OUTING_SATURDAY_WORK_NAME), names)
    }

    @Test
    fun `re-enqueues work scheduled at the same time in another time zone`() = runTest {
        existingWork[PRE_OUTING_FRIDAY_WORK_NAME] = workAt(REMINDER_TIME, WorkInfo.State.ENQUEUED, MADRID)

        scheduler.schedule(REMINDER_TIME)

        assertEquals(listOf(PRE_OUTING_FRIDAY_WORK_NAME, PRE_OUTING_SATURDAY_WORK_NAME), names)
        assertEquals(List(2) { ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE }, policies)
    }

    @Test
    fun `re-enqueues work scheduled at a different time`() = runTest {
        existingWork[PRE_OUTING_FRIDAY_WORK_NAME] = workAt(REMINDER_TIME, WorkInfo.State.ENQUEUED)

        scheduler.schedule(LATER_REMINDER_TIME)

        assertEquals(listOf(PRE_OUTING_FRIDAY_WORK_NAME, PRE_OUTING_SATURDAY_WORK_NAME), names)
        assertEquals(List(2) { ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE }, policies)
    }

    @Test
    fun `enqueues again when the work with the same time is finished`() = runTest {
        existingWork[PRE_OUTING_FRIDAY_WORK_NAME] = workAt(REMINDER_TIME, WorkInfo.State.CANCELLED)

        scheduler.schedule(REMINDER_TIME)

        assertEquals(listOf(PRE_OUTING_FRIDAY_WORK_NAME, PRE_OUTING_SATURDAY_WORK_NAME), names)
    }

    @Test
    fun `cancel cancels both unique works`() = runTest {
        scheduler.cancel()

        verify(exactly = 1) { workManager.cancelUniqueWork(PRE_OUTING_FRIDAY_WORK_NAME) }
        verify(exactly = 1) { workManager.cancelUniqueWork(PRE_OUTING_SATURDAY_WORK_NAME) }
    }
}
