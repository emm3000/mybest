package com.emm.mybest.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class UserPreferencesRepositoryImplTest {

    @Test
    fun `notifications are true by default`() = runTest {
        val repository = UserPreferencesRepositoryImpl(createStore(backgroundScope))

        assertEquals(true, repository.notificationsEnabled.first())
    }

    @Test
    fun `onboarding is not completed when no key is stored`() = runTest {
        val repository = UserPreferencesRepositoryImpl(createStore(backgroundScope))

        assertEquals(false, repository.onboardingCompleted.first())
    }

    @Test
    fun `onboarding completed follows the last stored value`() = runTest {
        val repository = UserPreferencesRepositoryImpl(createStore(backgroundScope))

        repository.updateOnboardingCompleted(true)
        assertEquals(true, repository.onboardingCompleted.first())
        repository.updateOnboardingCompleted(false)
        assertEquals(false, repository.onboardingCompleted.first())
    }

    private fun createStore(scope: CoroutineScope) = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = {
            File.createTempFile("prefs_test_", ".preferences_pb").also { it.deleteOnExit() }
        },
    )
}
