package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.repository.AppRestarter
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsBackupRestoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository = mockk<UserPreferencesRepository>()
    private val appRestarter = mockk<AppRestarter>(relaxUnitFun = true)

    private fun buildViewModel(restoreResult: Result<RestoreResult>): SettingsViewModel {
        val setupRepository = FakeSetupRepository()
        return SettingsViewModel(
            dependencies = SettingsDependencies(
                userPreferencesRepository = preferencesRepository,
                exportDatabaseBackupUseCase = mockk<ExportDatabaseBackupUseCase>(),
                restoreDatabaseBackupUseCase = mockk<RestoreDatabaseBackupUseCase> {
                    coEvery { this@mockk.invoke(any()) } returns restoreResult
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
                appRestarter = appRestarter,
            ),
        )
    }

    @Test
    fun `importing a backup that needs a restart announces it was restored`() = runTest {
        val viewModel = buildViewModel(Result.success(RestoreResult.RequiresRestart))

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))

        assertEquals(SettingsEffect.BackupRestored, viewModel.effect.first())
    }

    @Test
    fun `importing a backup that needs a restart relaunches the app once`() = runTest {
        val viewModel = buildViewModel(Result.success(RestoreResult.RequiresRestart))

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))
        advanceUntilIdle()

        verify(exactly = 1) { appRestarter.restart() }
    }

    @Test
    fun `the restored confirmation is announced before the app relaunches`() = runTest {
        val events = mutableListOf<Any>()
        every { appRestarter.restart() } answers { events += RESTART }
        val viewModel = buildViewModel(Result.success(RestoreResult.RequiresRestart))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { events += it }
        }

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))
        advanceUntilIdle()

        assertEquals(listOf(SettingsEffect.BackupRestored, RESTART), events)
    }

    @Test
    fun `importing a backup from another schema version announces it cannot be restored`() = runTest {
        val viewModel = buildViewModel(Result.success(RestoreResult.OtherSchemaVersion))

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))

        assertEquals(SettingsEffect.BackupFromOtherVersion, viewModel.effect.first())
    }

    @Test
    fun `importing a backup from another schema version does not relaunch the app`() = runTest {
        val viewModel = buildViewModel(Result.success(RestoreResult.OtherSchemaVersion))

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))
        advanceUntilIdle()

        verify(exactly = 0) { appRestarter.restart() }
    }

    @Test
    fun `a failed import announces the import failed and does not relaunch the app`() = runTest {
        val viewModel = buildViewModel(Result.failure(IllegalStateException("Invalid SQLite backup file")))

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))

        assertEquals(SettingsEffect.BackupImportFailed, viewModel.effect.first())
        advanceUntilIdle()
        verify(exactly = 0) { appRestarter.restart() }
    }

    @Test
    fun `a failed import without a message announces the import failed`() = runTest {
        val viewModel = buildViewModel(Result.failure(IllegalStateException()))

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))

        assertEquals(SettingsEffect.BackupImportFailed, viewModel.effect.first())
    }

    private companion object {
        const val RESTART = "restart"
    }
}
