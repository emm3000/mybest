package com.emm.mybest.domain.usecase.capture

import com.emm.mybest.testing.FakeNotificationCaptureRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveCaptureSummaryUseCaseTest {

    @Test
    fun `capture is off while no app is selected`() = runTest {
        val summary = ObserveCaptureSummaryUseCase(FakeNotificationCaptureRepository())().first()

        assertEquals(0, summary.selectedAppCount)
        assertFalse(summary.isActive)
    }

    @Test
    fun `capture is on and counts the selected apps`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf("a", "b"))

        val summary = ObserveCaptureSummaryUseCase(repository)().first()

        assertEquals(2, summary.selectedAppCount)
        assertTrue(summary.isActive)
    }
}
