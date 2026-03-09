package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyHabitEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DailyHabitDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: DailyHabitDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.dailyHabitDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observe_and_count_queries_return_expected_values() = runBlocking {
        val day1 = LocalDate(2026, 3, 8)
        val day2 = LocalDate(2026, 3, 9)
        dao.upsert(DailyHabitEntity(date = day1, ateHealthy = true, didExercise = true, notes = "good"))
        dao.upsert(DailyHabitEntity(date = day2, ateHealthy = false, didExercise = false, notes = null))

        val byDate = dao.observeByDate(day1).first()
        val all = dao.observeAll().first()
        val exerciseCount = dao.countExerciseDays()

        assertNotNull(byDate)
        assertEquals(true, byDate?.didExercise)
        assertEquals(2, all.size)
        assertEquals(day2, all.first().date)
        assertEquals(1, exerciseCount)
    }

    @Test
    fun getByDate_and_deleteByDate_work_correctly() = runBlocking {
        val day = LocalDate(2026, 3, 10)
        dao.upsert(DailyHabitEntity(date = day, ateHealthy = true, didExercise = false, notes = "x"))
        assertNotNull(dao.getByDate(day))

        dao.deleteByDate(day)

        assertNull(dao.getByDate(day))
    }
}
