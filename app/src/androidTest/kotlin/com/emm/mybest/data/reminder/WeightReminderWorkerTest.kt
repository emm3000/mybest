package com.emm.mybest.data.reminder

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.SetupRepository
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.reminder.IsPhotoReminderDueUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.Instant

private const val PHOTO_REMINDER_NOTIFICATION_ID = 1004
private val LIMA = TimeZone.of("America/Lima")
private val WEIGH_IN_MORNING = LocalDateTime(2026, 10, 23, 8, 0).toInstant(LIMA)

class WeightReminderWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        context = instrumentation.targetContext
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        notificationManager().cancelAll()
        stopKoin()
        startKoin {
            modules(
                module {
                    single<UserPreferencesRepository> { FakeUserPreferencesRepository(enabled = true) }
                    single { ReminderNotifier(context) }
                    single {
                        IsPhotoReminderDueUseCase(
                            FakeSetupRepository(Setup(isPhotoReminderEnabled = true)),
                            FakePhotoRepository(),
                            AppDayClock(FixedClock(WEIGH_IN_MORNING), LIMA),
                        )
                    }
                },
            )
        }
    }

    @After
    fun tearDown() = stopKoin()

    private fun notificationManager(): NotificationManager = context.getSystemService(NotificationManager::class.java)

    @Test
    fun doWork_posts_the_photo_notification_when_a_photo_is_due() = runBlocking {
        val worker = TestListenableWorkerBuilder.from(context, WeightReminderWorker::class.java).build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue(notificationManager().activeNotifications.any { it.id == PHOTO_REMINDER_NOTIFICATION_ID })
    }

    @Test
    fun doWork_returns_success_and_skips_notification_when_disabled() = runBlocking {
        stopKoin()
        startKoin {
            modules(
                module {
                    single<UserPreferencesRepository> { FakeUserPreferencesRepository(enabled = false) }
                },
            )
        }

        val worker = TestListenableWorkerBuilder.from(context, WeightReminderWorker::class.java).build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }
}

private class FakeUserPreferencesRepository(enabled: Boolean) : UserPreferencesRepository {

    override val notificationsEnabled: Flow<Boolean> = flowOf(enabled)

    override suspend fun updateNotificationsEnabled(enabled: Boolean) = Unit
}

private class FakeSetupRepository(private val setup: Setup) : SetupRepository {

    override fun observe(): Flow<Setup> = flowOf(setup)

    override suspend fun save(setup: Setup) = Unit
}

private class FakePhotoRepository : PhotoRepository {

    override fun getAllPhotos(): Flow<List<ProgressPhoto>> = flowOf(emptyList())

    override fun getPhotosByType(type: PhotoType): Flow<List<ProgressPhoto>> = flowOf(emptyList())

    override suspend fun savePhotos(photos: List<NewProgressPhoto>) = Unit

    override suspend fun deletePhoto(photoId: String) = Unit
}

private class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}
