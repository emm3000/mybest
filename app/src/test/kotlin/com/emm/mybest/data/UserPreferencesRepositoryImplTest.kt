package com.emm.mybest.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesRepositoryImplTest {

    @Test
    fun `isDarkMode is null by default`() = runTest {
        val repository = UserPreferencesRepositoryImpl(createStore(backgroundScope))

        val result = repository.isDarkMode.first()

        assertNull(result)
    }

    @Test
    fun `notifications and dynamic color are true by default`() = runTest {
        val repository = UserPreferencesRepositoryImpl(createStore(backgroundScope))

        assertEquals(true, repository.notificationsEnabled.first())
        assertEquals(true, repository.useDynamicColor.first())
    }

    @Test
    fun `updateDarkMode persists and emits value`() = runTest {
        val repository = UserPreferencesRepositoryImpl(createStore(backgroundScope))

        repository.updateDarkMode(enabled = true)

        assertEquals(true, repository.isDarkMode.first())
    }

    @Test
    fun `updateDynamicColor persists and emits value`() = runTest {
        val repository = UserPreferencesRepositoryImpl(createStore(backgroundScope))

        repository.updateDynamicColor(enabled = false)

        assertEquals(false, repository.useDynamicColor.first())
    }

    private fun createStore(scope: CoroutineScope) = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = {
            File.createTempFile("prefs_test_", ".preferences_pb").also { it.deleteOnExit() }
        },
    )
}
