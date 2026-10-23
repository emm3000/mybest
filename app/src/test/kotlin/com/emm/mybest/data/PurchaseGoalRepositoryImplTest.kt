package com.emm.mybest.data

import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.GoalContributionEntity
import com.emm.mybest.data.entities.PurchaseGoalDao
import com.emm.mybest.data.entities.PurchaseGoalEntity
import com.emm.mybest.domain.models.PurchaseGoal
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PurchaseGoalRepositoryImplTest {

    private val purchaseGoalDao = mockk<PurchaseGoalDao>(relaxUnitFun = true)
    private val repository = PurchaseGoalRepositoryImpl(purchaseGoalDao)

    @Test
    fun `observeGoal emits null when no goal is stored`() = runTest {
        every { purchaseGoalDao.observe() } returns flowOf(null)

        assertNull(repository.observeGoal().first())
    }

    @Test
    fun `saveGoal stores the goal in the single row`() = runTest {
        repository.saveGoal(PurchaseGoal(name = "Bicicleta", price = Money(150_000)))

        coVerify { purchaseGoalDao.upsert(PurchaseGoalEntity(name = "Bicicleta", priceCents = 150_000)) }
    }

    @Test
    fun `addContribution inserts a new contribution on the app day`() = runTest {
        coEvery { purchaseGoalDao.insertContribution(any()) } returns 1
        val friday = LocalDate(2026, 10, 23)

        repository.addContribution(friday, Money(5_000))

        coVerify { purchaseGoalDao.insertContribution(GoalContributionEntity(appDay = friday, amountCents = 5_000)) }
    }

    @Test
    fun `clearGoal removes the goal and its contributions together`() = runTest {
        repository.clearGoal()

        coVerify { purchaseGoalDao.clear() }
    }

    @Test
    fun `observeContributions maps stored rows to contributions`() = runTest {
        val friday = LocalDate(2026, 10, 23)
        every { purchaseGoalDao.observeContributions() } returns
            flowOf(listOf(GoalContributionEntity(id = 3, appDay = friday, amountCents = 2_500)))

        assertEquals(Money(2_500), repository.observeContributions().first().single().amount)
    }
}
