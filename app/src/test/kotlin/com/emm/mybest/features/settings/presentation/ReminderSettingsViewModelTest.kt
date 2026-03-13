package com.emm.mybest.features.settings.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.ExportDatabaseBackupUseCase
import com.emm.mybest.domain.usecase.RestoreDatabaseBackupUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository = mockk<UserPreferencesRepository>()
    private val exportUseCase = mockk<ExportDatabaseBackupUseCase>()
    private val restoreUseCase = mockk<RestoreDatabaseBackupUseCase>()

    @Test
    fun `state maps notifications preference`() = runTest {
        every { preferencesRepository.notificationsEnabled } returns flowOf(false)
        every { preferencesRepository.isDarkMode } returns flowOf(null)
        coEvery { preferencesRepository.updateDarkMode(any()) } returns Unit
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(Unit)
        val viewModel = ReminderSettingsViewModel(preferencesRepository, exportUseCase, restoreUseCase)

        viewModel.state.test {
            awaitItem()
            assertEquals(false, awaitItem().notificationsEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnNotificationsToggle updates preference`() = runTest {
        every { preferencesRepository.notificationsEnabled } returns flowOf(true)
        every { preferencesRepository.isDarkMode } returns flowOf(null)
        coEvery { preferencesRepository.updateDarkMode(any()) } returns Unit
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(Unit)
        val viewModel = ReminderSettingsViewModel(preferencesRepository, exportUseCase, restoreUseCase)

        viewModel.onIntent(ReminderSettingsIntent.OnNotificationsToggle(false))
        advanceUntilIdle()

        coVerify(exactly = 1) { preferencesRepository.updateNotificationsEnabled(false) }
    }

    @Test
    fun `OnExportBackup invokes export use case`() = runTest {
        every { preferencesRepository.notificationsEnabled } returns flowOf(true)
        every { preferencesRepository.isDarkMode } returns flowOf(null)
        coEvery { preferencesRepository.updateDarkMode(any()) } returns Unit
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(Unit)
        val viewModel = ReminderSettingsViewModel(preferencesRepository, exportUseCase, restoreUseCase)

        viewModel.onIntent(ReminderSettingsIntent.OnExportBackup("content://backup"))
        advanceUntilIdle()

        coVerify(exactly = 1) { exportUseCase.invoke("content://backup") }
    }

    @Test
    fun `OnImportBackup invokes restore use case`() = runTest {
        every { preferencesRepository.notificationsEnabled } returns flowOf(true)
        every { preferencesRepository.isDarkMode } returns flowOf(null)
        coEvery { preferencesRepository.updateDarkMode(any()) } returns Unit
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { exportUseCase.invoke(any()) } returns Result.success(Unit)
        coEvery { restoreUseCase.invoke(any()) } returns Result.success(Unit)
        val viewModel = ReminderSettingsViewModel(preferencesRepository, exportUseCase, restoreUseCase)

        viewModel.onIntent(ReminderSettingsIntent.OnImportBackup("content://backup"))
        advanceUntilIdle()

        coVerify(exactly = 1) { restoreUseCase.invoke("content://backup") }
    }
}
