package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.OutingDao
import com.emm.mybest.data.entities.OutingEntity
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.OutingKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OutingDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: OutingDao

    private val friday = LocalDate(2026, 10, 23)
    private val saturday = LocalDate(2026, 10, 24)

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.outingDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun second_insert_on_same_app_day_returns_minus_one_and_keeps_one_row() = runBlocking {
        val firstId = dao.insert(OutingEntity(appDay = friday, kind = OutingKind.PLANNED))
        val secondId = dao.insert(OutingEntity(appDay = friday, kind = OutingKind.UNPLANNED))

        val outings = dao.observeAll().first()
        assertTrue(firstId > 0)
        assertEquals(-1L, secondId)
        assertEquals(listOf(OutingEntity(id = firstId, appDay = friday, kind = OutingKind.PLANNED)), outings)
    }

    @Test
    fun observe_pending_excludes_checked_in_outings() = runBlocking {
        dao.insert(OutingEntity(appDay = friday, kind = OutingKind.UNPLANNED, spentCents = 5_000, mood = 3))
        dao.insert(OutingEntity(appDay = saturday, kind = OutingKind.UNPLANNED))

        val pendingDays = dao.observePending().first().map { it.appDay }

        assertEquals(listOf(saturday), pendingDays)
    }

    @Test
    fun recording_a_second_planned_outing_on_the_same_app_day_throws() = runBlocking {
        val repository = OutingRepositoryImpl(dao, database.wonNightDao(), database.noOutingAnswerDao())
        val limits = Limits(money = Money(12_000), drinks = 4, returnTime = LocalTime(2, 0))
        repository.recordPlanned(friday, limits)

        val failure = runCatching { repository.recordPlanned(friday, limits) }.exceptionOrNull()

        assertTrue(failure is IllegalStateException)
        assertEquals(1, dao.observeAll().first().size)
    }
}
