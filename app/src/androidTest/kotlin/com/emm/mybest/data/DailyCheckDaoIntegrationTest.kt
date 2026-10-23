package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.data.entities.DailyCheckDao
import com.emm.mybest.data.entities.DailyCheckEntity
import com.emm.mybest.domain.models.DailyCheckType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DailyCheckDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: DailyCheckDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.dailyCheckDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsert_replaces_check_for_same_app_day_and_type() = runBlocking {
        val appDay = LocalDate(2026, 10, 23)

        dao.upsert(DailyCheckEntity(appDay = appDay, type = DailyCheckType.GYM, isDone = true))
        dao.upsert(DailyCheckEntity(appDay = appDay, type = DailyCheckType.GYM, isDone = false))
        dao.upsert(DailyCheckEntity(appDay = appDay, type = DailyCheckType.ALCOHOL, isDone = true))

        val checks = dao.observeRange(appDay, appDay).first()
        assertEquals(
            listOf(
                DailyCheckEntity(appDay = appDay, type = DailyCheckType.ALCOHOL, isDone = true),
                DailyCheckEntity(appDay = appDay, type = DailyCheckType.GYM, isDone = false),
            ),
            checks,
        )
    }

    @Test
    fun observe_range_includes_both_ends_and_excludes_outside_days() = runBlocking {
        val from = LocalDate(2026, 10, 19)
        val to = LocalDate(2026, 10, 25)
        listOf(LocalDate(2026, 10, 18), from, to, LocalDate(2026, 10, 26)).forEach { day ->
            dao.upsert(DailyCheckEntity(appDay = day, type = DailyCheckType.DIET, isDone = true))
        }

        val days = dao.observeRange(from, to).first().map { it.appDay }

        assertEquals(listOf(from, to), days)
    }
}
