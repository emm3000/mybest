package com.emm.mybest.viewmodel

import app.cash.turbine.test
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val isDarkModeFlow = MutableStateFlow<Boolean?>(null)
    private val preferencesRepository = mockk<UserPreferencesRepository> {
        every { isDarkMode } returns isDarkModeFlow
        every { notificationsEnabled } returns MutableStateFlow(true)
    }

    @Test
    fun `state combines preferences flows`() = runTest {
        val viewModel = MainViewModel(preferencesRepository)

        viewModel.state.test {
            assertEquals(MainState(isDarkMode = null), awaitItem())

            isDarkModeFlow.value = true
            assertEquals(MainState(isDarkMode = true), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
