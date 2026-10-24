package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.repository.RestoreResult
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

class SettingsBackupRestoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository = mockk<UserPreferencesRepository>()

    private fun buildViewModel(restoreResult: RestoreResult): SettingsViewModel {
        val setupRepository = FakeSetupRepository()
        return SettingsViewModel(
            dependencies = SettingsDependencies(
                userPreferencesRepository = preferencesRepository,
                exportDatabaseBackupUseCase = mockk<ExportDatabaseBackupUseCase>(),
                restoreDatabaseBackupUseCase = mockk<RestoreDatabaseBackupUseCase> {
                    coEvery { this@mockk.invoke(any()) } returns Result.success(restoreResult)
                },
                goalsEditor = SettingsGoalsEditor(
                    ObserveSetupUseCase(setupRepository),
                    UpdateSetupUseCase(setupRepository),
                ),
                remindersEditor = SettingsRemindersEditor(
                    ObserveSetupUseCase(setupRepository),
                    UpdateSetupUseCase(setupRepository),
                ),
                rowSources = SettingsRowSources(
                    ObserveDebtSummaryUseCase(FakeDebtRepository()),
                    FakePurchaseGoalRepository(),
                    ObserveCaptureSummaryUseCase(FakeNotificationCaptureRepository()),
                ),
            ),
        )
    }

    @Test
    fun `importing a backup that needs a restart announces it was restored`() = runTest {
        val viewModel = buildViewModel(RestoreResult.RequiresRestart)

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))

        assertEquals(SettingsEffect.BackupRestored, viewModel.effect.first())
    }

    @Test
    fun `importing a backup from another schema version announces it cannot be restored`() = runTest {
        val viewModel = buildViewModel(RestoreResult.OtherSchemaVersion)

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))

        assertEquals(SettingsEffect.BackupFromOtherVersion, viewModel.effect.first())
    }
}
