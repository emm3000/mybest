package com.emm.mybest.data

import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.GoalContributionEntity
import com.emm.mybest.data.entities.PurchaseGoalDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.data.mappers.toEntity
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class PurchaseGoalRepositoryImpl(
    private val purchaseGoalDao: PurchaseGoalDao,
) : PurchaseGoalRepository {

    override fun observeGoal(): Flow<PurchaseGoal?> = purchaseGoalDao.observe().map { it?.toDomain() }

    override suspend fun saveGoal(goal: PurchaseGoal) {
        purchaseGoalDao.upsert(goal.toEntity())
    }

    override suspend fun clearGoal() {
        purchaseGoalDao.clear()
    }

    override fun observeContributions(): Flow<List<GoalContribution>> =
        purchaseGoalDao.observeContributions().map { list -> list.map { it.toDomain() } }

    override suspend fun addContribution(appDay: LocalDate, amount: Money) {
        purchaseGoalDao.insertContribution(GoalContributionEntity(appDay = appDay, amountCents = amount.cents))
    }

    override suspend fun deleteContribution(id: Long) {
        purchaseGoalDao.deleteContribution(id)
    }
}
