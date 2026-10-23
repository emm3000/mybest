package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.PurchaseGoal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PurchaseGoalDaoIntegrationTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun clearing_the_purchase_goal_removes_its_contributions() = runBlocking {
        val goalRepository = PurchaseGoalRepositoryImpl(database.purchaseGoalDao())
        goalRepository.saveGoal(PurchaseGoal("Bicicleta", Money(150_000)))
        goalRepository.addContribution(LocalDate(2026, 10, 23), Money(5_000))

        goalRepository.clearGoal()

        assertNull(goalRepository.observeGoal().first())
        assertTrue(goalRepository.observeContributions().first().isEmpty())
    }
}
