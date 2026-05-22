package com.emm.mybest.data.reminder

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.DailySlotTimes
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class WeightReminderWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        startKoin {
            modules(
                module {
                    single<UserPreferencesRepository> { FakeUserPreferencesRepository(enabled = true) }
                },
            )
        }
    }

    @After
    fun tearDown() = stopKoin()

    @Test
    fun doWork_returns_success_when_enabled() = runBlocking {
        val worker = TestListenableWorkerBuilder.from(context, WeightReminderWorker::class.java).build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
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

    override val weightReminderTime: Flow<LocalTime?> = flowOf(null)

    override val dailySlotTimes: Flow<DailySlotTimes> = flowOf(DailySlotTimes(emptyMap()))

    override suspend fun updateNotificationsEnabled(enabled: Boolean) = Unit

    override suspend fun setReminderTime(time: LocalTime?) = Unit

    override suspend fun setDailySlotTime(slot: DailySlot, time: LocalTime) = Unit
}
