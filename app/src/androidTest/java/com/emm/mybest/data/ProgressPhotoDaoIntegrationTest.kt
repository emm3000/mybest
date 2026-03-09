package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProgressPhotoDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ProgressPhotoDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.progressPhotoDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun type_queries_and_ordering_return_expected_data() = runBlocking {
        val older = ProgressPhotoEntity(
            id = "p1",
            date = LocalDate(2026, 3, 7),
            type = com.emm.mybest.data.entities.PhotoType.FACE,
            photoPath = "/tmp/1.jpg",
        )
        val newer = ProgressPhotoEntity(
            id = "p2",
            date = LocalDate(2026, 3, 9),
            type = com.emm.mybest.data.entities.PhotoType.FACE,
            photoPath = "/tmp/2.jpg",
        )
        val body = ProgressPhotoEntity(
            id = "p3",
            date = LocalDate(2026, 3, 8),
            type = com.emm.mybest.data.entities.PhotoType.BODY,
            photoPath = "/tmp/3.jpg",
        )
        dao.insertAll(listOf(older, newer, body))

        val all = dao.observeAll().first()
        val face = dao.observeByType(com.emm.mybest.data.entities.PhotoType.FACE).first()
        val firstFace = dao.getFirstByType(com.emm.mybest.data.entities.PhotoType.FACE)
        val lastFace = dao.getLastByType(com.emm.mybest.data.entities.PhotoType.FACE)

        assertEquals(3, all.size)
        assertEquals("p2", all.first().id)
        assertEquals(2, face.size)
        assertNotNull(firstFace)
        assertNotNull(lastFace)
        assertEquals("p1", firstFace?.id)
        assertEquals("p2", lastFace?.id)
    }

    @Test
    fun deleteById_removes_entry() = runBlocking {
        val photo = ProgressPhotoEntity(
            id = "p4",
            date = LocalDate(2026, 3, 10),
            type = com.emm.mybest.data.entities.PhotoType.ABDOMEN,
            photoPath = "/tmp/4.jpg",
        )
        dao.insert(photo)
        assertNotNull(dao.getFirstByType(com.emm.mybest.data.entities.PhotoType.ABDOMEN))

        dao.deleteById("p4")

        assertNull(dao.getFirstByType(com.emm.mybest.data.entities.PhotoType.ABDOMEN))
    }

    @Test
    fun empty_state_queries_return_empty_or_null() = runBlocking {
        val all = dao.observeAll().first()
        val byType = dao.observeByType(com.emm.mybest.data.entities.PhotoType.FACE).first()
        val first = dao.getFirstByType(com.emm.mybest.data.entities.PhotoType.FACE)
        val last = dao.getLastByType(com.emm.mybest.data.entities.PhotoType.FACE)

        assertTrue(all.isEmpty())
        assertTrue(byType.isEmpty())
        assertNull(first)
        assertNull(last)
    }
}
