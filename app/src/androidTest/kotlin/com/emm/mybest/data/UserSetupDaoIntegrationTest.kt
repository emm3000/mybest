package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.data.entities.UserSetupDao
import com.emm.mybest.data.entities.UserSetupEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class UserSetupDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: UserSetupDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.userSetupDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observe_emits_null_before_any_setup_is_saved() = runBlocking {
        assertNull(dao.observe().first())
    }

    @Test
    fun upsert_keeps_a_single_setup_row() = runBlocking {
        dao.upsert(UserSetupEntity(monthlyOutingLimit = 2))
        dao.upsert(UserSetupEntity(monthlyOutingLimit = 3, typicalOutingCostCents = 15_000L))

        assertEquals(
            UserSetupEntity(monthlyOutingLimit = 3, typicalOutingCostCents = 15_000L),
            dao.observe().first(),
        )
    }
}
