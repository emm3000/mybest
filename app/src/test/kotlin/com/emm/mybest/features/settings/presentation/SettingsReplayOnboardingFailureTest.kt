package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import com.emm.mybest.domain.usecase.capture.ObserveCaptureSummaryUseCase
import com.emm.mybest.domain.usecase.debts.ObserveDebtSummaryUseCase
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeNotificationCaptureRepository
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SettingsReplayOnboardingFailureTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository = mockk<UserPreferencesRepository> {
        coEvery { updateOnboardingCompleted(any()) } throws IOException("disk full")
    }

    private fun buildViewModel(setupRepository: FakeSetupRepository = FakeSetupRepository()) = SettingsViewModel(
        userPreferencesRepository = preferencesRepository,
        exportDatabaseBackupUseCase = mockk<ExportDatabaseBackupUseCase>(),
        restoreDatabaseBackupUseCase = mockk<RestoreDatabaseBackupUseCase>(),
        goalsEditor = SettingsGoalsEditor(ObserveSetupUseCase(setupRepository), UpdateSetupUseCase(setupRepository)),
        remindersEditor = SettingsRemindersEditor(
            ObserveSetupUseCase(setupRepository),
            UpdateSetupUseCase(setupRepository),
        ),
        rowSources = SettingsRowSources(
            ObserveDebtSummaryUseCase(FakeDebtRepository()),
            FakePurchaseGoalRepository(),
            ObserveCaptureSummaryUseCase(FakeNotificationCaptureRepository()),
        ),
    )

    @Test
    fun `a failed replay write shows an error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnReplayOnboarding)

        assertEquals(SettingsEffect.ShowError("disk full"), viewModel.effect.first())
    }
}
