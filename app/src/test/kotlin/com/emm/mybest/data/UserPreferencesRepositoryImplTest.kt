package com.emm.mybest.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.DailySlotTimes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
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
    fun `dailySlotTimes returns defaults when nothing is stored`() = runTest {
        val repository = UserPreferencesRepositoryImpl(createStore(backgroundScope))

        val emitted = repository.dailySlotTimes.first()

        assertEquals(DailySlotTimes.DEFAULT_TIMES, emitted.times)
    }

    @Test
    fun `setDailySlotTime overrides only the targeted slot`() = runTest {
        val repository = UserPreferencesRepositoryImpl(createStore(backgroundScope))
        val override = LocalTime(8, 15)

        repository.setDailySlotTime(DailySlot.BREAKFAST, override)
        val emitted = repository.dailySlotTimes.first()

        assertEquals(override, emitted[DailySlot.BREAKFAST])
        assertEquals(DailySlotTimes.DEFAULT_TIMES[DailySlot.LUNCH], emitted[DailySlot.LUNCH])
    }

    private fun createStore(scope: CoroutineScope) = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = {
            File.createTempFile("prefs_test_", ".preferences_pb").also { it.deleteOnExit() }
        },
    )
}
