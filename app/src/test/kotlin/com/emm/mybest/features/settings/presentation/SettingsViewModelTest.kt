package com.emm.mybest.features.settings.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.repository.RestoreResult
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.UpdateDefaultReminderTimeUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository = mockk<UserPreferencesRepository>()
    private val exportUseCase = mockk<ExportDatabaseBackupUseCase>()
    private val restoreUseCase = mockk<RestoreDatabaseBackupUseCase>()
    private val updateDefaultReminderTimeUseCase = mockk<UpdateDefaultReminderTimeUseCase>()

    private fun buildViewModel(
        versionName: String = "1.0",
        versionCode: Int = 1,
    ): SettingsViewModel = SettingsViewModel(
        userPreferencesRepository = preferencesRepository,
        exportDatabaseBackupUseCase = exportUseCase,
        restoreDatabaseBackupUseCase = restoreUseCase,
        updateDefaultReminderTimeUseCase = updateDefaultReminderTimeUseCase,
        appVersionName = versionName,
        appVersionCode = versionCode,
    )

    private fun stubDefaults() {
        every { preferencesRepository.notificationsEnabled } returns flowOf(true)
        every { preferencesRepository.weightReminderTime } returns flowOf(null)
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(RestoreResult.RequiresRestart)
        coEvery { updateDefaultReminderTimeUseCase.invoke(any()) } returns Unit
    }

    @Test
    fun `initial state exposes correct appVersionLabel`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel(versionName = "1.0", versionCode = 1)

        viewModel.state.test {
            val item = awaitItem()
            assertEquals("v1.0 · 1", item.appVersionLabel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state maps notifications preference`() = runTest {
        every { preferencesRepository.notificationsEnabled } returns flowOf(false)
        every { preferencesRepository.weightReminderTime } returns flowOf(null)
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(RestoreResult.RequiresRestart)
        coEvery { updateDefaultReminderTimeUseCase.invoke(any()) } returns Unit
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            assertEquals(false, awaitItem().notificationsEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnNotificationsToggle updates preference`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnNotificationsToggle(false))
        advanceUntilIdle()

        coVerify(exactly = 1) { preferencesRepository.updateNotificationsEnabled(false) }
    }

    @Test
    fun `OnExportBackup invokes export use case`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnExportBackup("content://backup"))
        advanceUntilIdle()

        coVerify(exactly = 1) { exportUseCase.invoke("content://backup") }
    }

    @Test
    fun `OnImportBackup invokes restore use case`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnImportBackup("content://backup"))
        advanceUntilIdle()

        coVerify(exactly = 1) { restoreUseCase.invoke("content://backup") }
    }

    @Test
    fun `OnDefaultReminderTimeChange invokes update use case with LocalTime`() = runTest {
        stubDefaults()
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnDefaultReminderTimeChange(7, 30))
        advanceUntilIdle()

        coVerify(exactly = 1) { updateDefaultReminderTimeUseCase.invoke(LocalTime(7, 30)) }
    }

    @Test
    fun `OnWeightReminderToggleOff invokes update use case with null`() = runTest {
        every { preferencesRepository.notificationsEnabled } returns flowOf(true)
        every { preferencesRepository.weightReminderTime } returns flowOf(LocalTime(8, 0))
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(RestoreResult.RequiresRestart)
        coEvery { updateDefaultReminderTimeUseCase.invoke(any()) } returns Unit
        val viewModel = buildViewModel()

        viewModel.onIntent(SettingsIntent.OnWeightReminderToggleOff)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateDefaultReminderTimeUseCase.invoke(null) }
    }
}
