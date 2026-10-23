package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.DebtDao
import com.emm.mybest.data.entities.DebtEntity
import com.emm.mybest.data.entities.DebtPaymentEntity
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.models.PurchaseGoal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DebtDaoIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: DebtDao

    private val october = YearMonthValue(2026, 10)
    private val debt = DebtEntity(name = "Tarjeta", balanceCents = 50_000, installmentCents = 20_000, paymentDay = 15)

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.debtDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun marking_the_same_month_paid_twice_lowers_the_balance_once() = runBlocking {
        val repository = DebtRepositoryImpl(dao)
        val id = dao.insert(debt)

        repository.markInstallmentPaid(id, october)
        repository.markInstallmentPaid(id, october)

        assertEquals(30_000L, dao.findById(id)?.balanceCents)
        assertEquals(listOf(DebtPayment(id, october, Money(20_000))), repository.observePayments().first())
    }

    @Test
    fun deleting_a_debt_removes_its_payments() = runBlocking {
        val id = dao.insert(debt)
        dao.insertPayment(DebtPaymentEntity(debtId = id, month = october, amountCents = 20_000))

        dao.deleteById(id)

        assertTrue(dao.observePayments().first().isEmpty())
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
