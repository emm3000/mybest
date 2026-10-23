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

    private fun createStore(scope: CoroutineScope) = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = {
            File.createTempFile("prefs_test_", ".preferences_pb").also { it.deleteOnExit() }
        },
    )
}
