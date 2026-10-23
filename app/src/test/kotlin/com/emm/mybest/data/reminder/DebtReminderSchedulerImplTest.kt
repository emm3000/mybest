package com.emm.mybest.data.reminder

import androidx.work.PeriodicWorkRequest
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
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val NINE_IN_THE_MORNING = LocalDateTime(2026, 10, 23, 9, 0).toInstant(LIMA)

class DebtReminderSchedulerImplTest {

    private val workManager = mockk<WorkManager>(relaxed = true)
    private val names = mutableListOf<String>()
    private val requests = mutableListOf<PeriodicWorkRequest>()
    private val scheduler = DebtReminderSchedulerImpl(
        workManager,
        ReminderDelays(FixedClock(NINE_IN_THE_MORNING), LIMA),
    )

    init {
        every { workManager.enqueueUniquePeriodicWork(capture(names), any(), capture(requests)) } returns mockk()
        every { workManager.getWorkInfosForUniqueWorkFlow(DEBT_REMINDER_WORK_NAME) } returns flowOf(emptyList())
    }

    @Test
    fun `schedules the debt worker daily under its own work name`() = runTest {
        scheduler.schedule(LocalTime(10, 0))

        assertEquals(listOf(DEBT_REMINDER_WORK_NAME), names)
        assertEquals(DebtReminderWorker::class.java.name, requests.single().workSpec.workerClassName)
    }

    @Test
    fun `cancel cancels the unique work`() = runTest {
        scheduler.cancel()

        verify(exactly = 1) { workManager.cancelUniqueWork(DEBT_REMINDER_WORK_NAME) }
    }
}
