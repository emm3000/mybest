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
import kotlinx.datetime.DayOfWeek
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
private val FRIDAY_AT_NINE = LocalDateTime(2026, 10, 23, 9, 0).toInstant(LIMA)
private val NINE = LocalTime(9, 0)
private val TEN = LocalTime(10, 0)
private val EIGHT = LocalTime(8, 0)
private const val HOURS_TO_LATER_TODAY = 1L
private const val HOURS_TO_EARLIER_TODAY_NEXT_WEEK = 7L * 24 - 1
private const val HOURS_TO_SUNDAY = 2L * 24

class WeightReminderSchedulerImplTest {

    private val workManager = mockk<WorkManager>(relaxed = true)
    private val names = mutableListOf<String>()
    private val policies = mutableListOf<ExistingPeriodicWorkPolicy>()
    private val requests = mutableListOf<PeriodicWorkRequest>()
    private val existingWork = mutableListOf<WorkInfo>()
    private val scheduler = WeightReminderSchedulerImpl(workManager, ReminderDelays(FixedClock(FRIDAY_AT_NINE), LIMA))

    init {
        every {
            workManager.enqueueUniquePeriodicWork(capture(names), capture(policies), capture(requests))
        } returns mockk(relaxed = true)
        every { workManager.getWorkInfosForUniqueWorkFlow(WEIGHT_REMINDER_WORK_NAME) } answers {
            flowOf(existingWork.toList())
        }
    }

    private fun workTagged(vararg tags: String) = WorkInfo(UUID.randomUUID(), WorkInfo.State.ENQUEUED, tags.toSet())

    private fun hoursOfDelay(): Long = TimeUnit.MILLISECONDS.toHours(requests.last().workSpec.initialDelay)

    @Test
    fun `schedules one unique weekly work with the weight worker`() = runTest {
        scheduler.schedule(DayOfWeek.FRIDAY, TEN)

        val spec = requests.single().workSpec
        assertEquals(listOf(WEIGHT_REMINDER_WORK_NAME), names)
        assertEquals(TimeUnit.DAYS.toMillis(7), spec.intervalDuration)
        assertEquals(WeightReminderWorker::class.java.name, spec.workerClassName)
    }

    @Test
    fun `on the weigh-in day before the time it waits until that time`() = runTest {
        scheduler.schedule(DayOfWeek.FRIDAY, TEN)

        assertEquals(HOURS_TO_LATER_TODAY, hoursOfDelay())
    }

    @Test
    fun `on the weigh-in day after the time it waits until next week`() = runTest {
        scheduler.schedule(DayOfWeek.FRIDAY, EIGHT)

        assertEquals(HOURS_TO_EARLIER_TODAY_NEXT_WEEK, hoursOfDelay())
    }

    @Test
    fun `waits until the next weigh-in day`() = runTest {
        scheduler.schedule(DayOfWeek.SUNDAY, NINE)

        assertEquals(HOURS_TO_SUNDAY, hoursOfDelay())
    }

    @Test
    fun `cancels and re-enqueues so a changed day takes effect`() = runTest {
        scheduler.schedule(DayOfWeek.FRIDAY, TEN)
        scheduler.schedule(DayOfWeek.SUNDAY, NINE)

        assertEquals(List(2) { ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE }, policies)
        assertEquals(HOURS_TO_SUNDAY, hoursOfDelay())
    }

    @Test
    fun `does not enqueue when already scheduled on the same day and time`() = runTest {
        existingWork += workTagged(reminderDayTimeTag(DayOfWeek.FRIDAY, TEN, LIMA))

        scheduler.schedule(DayOfWeek.FRIDAY, TEN)

        assertTrue(names.isEmpty())
    }

    @Test
    fun `re-enqueues work scheduled at the same time on another day`() = runTest {
        existingWork += workTagged(reminderDayTimeTag(DayOfWeek.MONDAY, TEN, LIMA))

        scheduler.schedule(DayOfWeek.FRIDAY, TEN)

        assertEquals(listOf(WEIGHT_REMINDER_WORK_NAME), names)
    }

    @Test
    fun `replaces the untagged daily work`() = runTest {
        existingWork += workTagged()

        scheduler.schedule(DayOfWeek.FRIDAY, TEN)

        assertEquals(listOf(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE), policies)
    }

    @Test
    fun `cancel cancels the unique work`() = runTest {
        scheduler.cancel()

        verify(exactly = 1) { workManager.cancelUniqueWork(WEIGHT_REMINDER_WORK_NAME) }
    }
}
