package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DailyWeightDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: DailyWeightDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.dailyWeightDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsert_replaces_entry_for_same_date_and_observe_returns_sorted() = runBlocking {
        val day1 = LocalDate(2026, 3, 8)
        val day2 = LocalDate(2026, 3, 9)

        dao.upsert(DailyWeightEntity(id = "w1", date = day2, weight = 71.0f))
        dao.upsert(DailyWeightEntity(id = "w2", date = day1, weight = 72.0f))
        dao.upsert(DailyWeightEntity(id = "w3", date = day1, weight = 70.5f))

        val all = dao.observeAllOrdered().first()
        assertEquals(2, all.size)
        assertEquals(day1, all[0].date)
        assertEquals(70.5f, all[0].weight)
        assertEquals(day2, all[1].date)
    }

    @Test
    fun getByDate_getLatest_and_deleteByDate_work_correctly() = runBlocking {
        val day1 = LocalDate(2026, 3, 8)
        val day2 = LocalDate(2026, 3, 9)
        dao.upsert(DailyWeightEntity(id = "w1", date = day1, weight = 72.0f))
        dao.upsert(DailyWeightEntity(id = "w2", date = day2, weight = 71.0f))

        assertNotNull(dao.getByDate(day1))
        assertEquals(day2, dao.getLatest()?.date)

        dao.deleteByDate(day1)
        assertNull(dao.getByDate(day1))
    }
}
