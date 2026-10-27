package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.data.entities.NoteDao
import com.emm.mybest.data.entities.NoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private val THURSDAY = LocalDate(2026, 11, 5)
private val FRIDAY = LocalDate(2026, 11, 6)
private val SATURDAY = LocalDate(2026, 11, 7)
private val SUNDAY = LocalDate(2026, 11, 8)
private const val FRIDAY_NIGHT_MS = 1_794_022_200_000L
private const val SATURDAY_NIGHT_MS = 1_794_108_600_000L

class NoteDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = database.noteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeRange_includes_both_end_days_and_excludes_the_days_outside() = runBlocking {
        listOf(THURSDAY, FRIDAY, SATURDAY, SUNDAY).forEach { insert(it, FRIDAY_NIGHT_MS, it.toString()) }

        val days = noteDao.observeRange(FRIDAY, SATURDAY).first().map { it.appDay }

        assertEquals(setOf(FRIDAY, SATURDAY), days.toSet())
    }

    @Test
    fun observeRange_orders_by_creation_instant_then_id_newest_first() = runBlocking {
        val first = insert(FRIDAY, FRIDAY_NIGHT_MS, "primera")
        val second = insert(FRIDAY, FRIDAY_NIGHT_MS, "segunda")
        val latest = insert(FRIDAY, SATURDAY_NIGHT_MS, "última")

        val ids = noteDao.observeRange(FRIDAY, FRIDAY).first().map { it.id }

        assertEquals(listOf(latest, second, first), ids)
    }

    @Test
    fun updateText_returns_one_for_a_stored_note_and_zero_for_an_unknown_id() = runBlocking {
        val id = insert(FRIDAY, FRIDAY_NIGHT_MS, "antes")

        assertEquals(1, noteDao.updateText(id, "después"))
        assertEquals(0, noteDao.updateText(id + 1, "nada"))
        assertEquals("después", noteDao.observeRange(FRIDAY, FRIDAY).first().single().text)
    }

    @Test
    fun delete_removes_only_that_row() = runBlocking {
        val kept = insert(FRIDAY, FRIDAY_NIGHT_MS, "queda")
        val removed = insert(FRIDAY, SATURDAY_NIGHT_MS, "se va")

        noteDao.delete(removed)

        assertEquals(listOf(kept), noteDao.observeRange(FRIDAY, FRIDAY).first().map { it.id })
    }

    private suspend fun insert(appDay: LocalDate, createdAtEpochMs: Long, text: String): Long =
        noteDao.insert(NoteEntity(appDay = appDay, createdAtEpochMs = createdAtEpochMs, text = text))
}
