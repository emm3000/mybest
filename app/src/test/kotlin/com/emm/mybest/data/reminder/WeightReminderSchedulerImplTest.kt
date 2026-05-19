package com.emm.mybest.data.reminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightReminderSchedulerImplTest {

    private val workManager = mockk<WorkManager>(relaxed = true)
    private val scheduler = WeightReminderSchedulerImpl(workManager)

    @Test
    fun `schedule calls enqueueUniquePeriodicWork with correct work name`() = runTest {
        val workNameSlot = slot<String>()
        val policySlot = slot<ExistingPeriodicWorkPolicy>()
        val requestSlot = slot<PeriodicWorkRequest>()
        every {
            workManager.enqueueUniquePeriodicWork(
                capture(workNameSlot),
                capture(policySlot),
                capture(requestSlot),
            )
        } returns mockk(relaxed = true)

        scheduler.schedule(LocalTime(8, 0))

        assertEquals(WEIGHT_REMINDER_WORK_NAME, workNameSlot.captured)
        assertEquals(ExistingPeriodicWorkPolicy.UPDATE, policySlot.captured)
    }

    @Test
    fun `cancel calls cancelUniqueWork with correct work name`() = runTest {
        val workNameSlot = slot<String>()
        every { workManager.cancelUniqueWork(capture(workNameSlot)) } returns mockk(relaxed = true)

        scheduler.cancel()

        assertEquals(WEIGHT_REMINDER_WORK_NAME, workNameSlot.captured)
        verify(exactly = 1) { workManager.cancelUniqueWork(WEIGHT_REMINDER_WORK_NAME) }
    }
}
