package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.usecase.capture.ObserveCaptureSummaryUseCase
import com.emm.mybest.domain.usecase.debts.ObserveDebtSummaryUseCase
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeNotificationCaptureRepository
import com.emm.mybest.testing.FakePurchaseGoalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsRowSourcesTest {

    private fun sources(captureRepository: FakeNotificationCaptureRepository) = SettingsRowSources(
        ObserveDebtSummaryUseCase(FakeDebtRepository()),
        FakePurchaseGoalRepository(),
        ObserveCaptureSummaryUseCase(captureRepository),
    )

    @Test
    fun `capture is off while no app is selected`() = runTest {
        val rows = sources(FakeNotificationCaptureRepository()).observe().first()

        assertEquals(SettingsCaptureState(isActive = false, selectedAppCount = 0), rows.capture)
    }

    @Test
    fun `capture is on with the selected app count`() = runTest {
        val repository = FakeNotificationCaptureRepository(selectedApps = setOf("a", "b"))

        val rows = sources(repository).observe().first()

        assertEquals(SettingsCaptureState(isActive = true, selectedAppCount = 2), rows.capture)
    }
}
