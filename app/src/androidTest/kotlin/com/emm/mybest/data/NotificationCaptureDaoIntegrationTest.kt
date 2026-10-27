package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.data.entities.CapturedNotificationEntity
import com.emm.mybest.domain.models.PostedNotification
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun the_lookup_returns_the_newest_row_of_the_key_and_breaks_ties_by_id() = runBlocking {
        repository.saveCaptured(posted("k1", "old", 1_000))
        repository.saveCaptured(posted("k1", "newest", 3_000))
        repository.saveCaptured(posted("k1", "same time, later id", 3_000))
        repository.saveCaptured(posted("k2", "other key", 9_000))

        assertEquals("same time, later id", repository.latestCapturedWithKey("k1")?.text)
    }

    @Test
    fun the_lookup_returns_null_for_an_unknown_key() = runBlocking {
        repository.saveCaptured(posted("k1", "text", 1_000))

        assertNull(repository.latestCapturedWithKey("unknown"))
    }

    @Test
    fun the_lookup_never_returns_a_row_without_a_key() = runBlocking {
        database.notificationCaptureDao().insertCaptured(
            CapturedNotificationEntity(
                packageName = YAPE,
                title = "Yape",
                text = "stored before keys",
                postedAtEpochMs = 1_000,
                notificationKey = null,
            ),
        )

        assertNull(repository.latestCapturedWithKey("k1"))
    }

    private fun posted(key: String, text: String, postedAtEpochMs: Long) =
        PostedNotification(YAPE, key, "Yape", text, Instant.fromEpochMilliseconds(postedAtEpochMs))
}
