package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.domain.models.PostedNotification
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

private const val YAPE = "com.bcp.innovacxion.yapeapp"
private const val PLIN = "com.interbank.plin"

class NotificationCaptureDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: NotificationCaptureRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = NotificationCaptureRepositoryImpl(database.notificationCaptureDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun selecting_an_app_twice_keeps_one_selection() = runBlocking {
        repository.setAppSelected(YAPE, isSelected = true)
        repository.setAppSelected(YAPE, isSelected = true)
        repository.setAppSelected(PLIN, isSelected = true)
        repository.setAppSelected(PLIN, isSelected = false)

        assertEquals(setOf(YAPE), repository.observeSelectedApps().first())
    }

    @Test
    fun captured_notifications_come_back_newest_first_and_unmodified() = runBlocking {
        repository.saveCaptured(PostedNotification(YAPE, "key", "Yape", "Te yapearon S/ 25.00", Instant.fromEpochMilliseconds(1_000)))
        repository.saveCaptured(PostedNotification(PLIN, "key", null, " Recibiste S/ 10.00 \n", Instant.fromEpochMilliseconds(2_000)))

        val captured = repository.observeCaptured().first()

        assertEquals(listOf(PLIN, YAPE), captured.map { it.packageName })
        assertEquals(" Recibiste S/ 10.00 \n", captured.first().text)
    }

    @Test
    fun deleting_all_captured_keeps_the_selected_apps() = runBlocking {
        repository.setAppSelected(YAPE, isSelected = true)
        repository.saveCaptured(PostedNotification(YAPE, "key", "Yape", "Te yapearon S/ 25.00", Instant.fromEpochMilliseconds(1_000)))

        repository.deleteAllCaptured()

        assertTrue(repository.observeCaptured().first().isEmpty())
        assertEquals(setOf(YAPE), repository.observeSelectedApps().first())
    }
}
