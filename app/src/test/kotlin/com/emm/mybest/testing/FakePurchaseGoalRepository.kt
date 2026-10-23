package com.emm.mybest.testing

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate

class FakePurchaseGoalRepository(
    initialGoal: PurchaseGoal? = null,
    initialContributions: List<GoalContribution> = emptyList(),
) : PurchaseGoalRepository {

    private val goal = MutableStateFlow(initialGoal)
    private val contributions = MutableStateFlow(initialContributions)

    override fun observeGoal(): Flow<PurchaseGoal?> = goal

    override suspend fun saveGoal(goal: PurchaseGoal) {
        this.goal.value = goal
    }

    override suspend fun clearGoal() {
        goal.value = null
        contributions.value = emptyList()
    }

    override fun observeContributions(): Flow<List<GoalContribution>> = contributions

    override suspend fun addContribution(appDay: LocalDate, amount: Money) {
        contributions.value = contributions.value + GoalContribution(contributions.value.size + 1L, appDay, amount)
    }

    override suspend fun deleteContribution(id: Long) {
        contributions.value = contributions.value.filterNot { it.id == id }
    }
}
