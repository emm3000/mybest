package com.emm.mybest.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

class UserPreferencesRepositoryInstrumentedTest {

    private lateinit var repository: UserPreferencesRepositoryImpl
    private lateinit var scope: CoroutineScope

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = {
                File.createTempFile("prefs_instrumented_", ".preferences_pb", context.cacheDir).also {
                    it.deleteOnExit()
                }
            },
        )
        repository = UserPreferencesRepositoryImpl(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun default_values_are_expected() = runBlocking {
        assertNull(repository.isDarkMode.first())
        assertEquals(true, repository.notificationsEnabled.first())
        assertEquals(true, repository.useDynamicColor.first())
    }

    @Test
    fun updates_are_persisted_in_flow() = runBlocking {
        repository.updateDarkMode(enabled = true)
        repository.updateDynamicColor(enabled = false)

        assertEquals(true, repository.isDarkMode.first())
        assertEquals(false, repository.useDynamicColor.first())
    }
}
