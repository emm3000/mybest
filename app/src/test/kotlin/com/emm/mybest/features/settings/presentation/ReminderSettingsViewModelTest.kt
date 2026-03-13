package com.emm.mybest.features.settings.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.repository.UserPreferencesRepository
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

    @Test
    fun `state maps notifications preference`() = runTest {
        every { preferencesRepository.notificationsEnabled } returns flowOf(false)
        every { preferencesRepository.isDarkMode } returns flowOf(null)
        coEvery { preferencesRepository.updateDarkMode(any()) } returns Unit
        coEvery { preferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        val viewModel = ReminderSettingsViewModel(preferencesRepository)

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
        val viewModel = ReminderSettingsViewModel(preferencesRepository)

        viewModel.onIntent(ReminderSettingsIntent.OnNotificationsToggle(false))
        advanceUntilIdle()

        coVerify(exactly = 1) { preferencesRepository.updateNotificationsEnabled(false) }
    }
}
