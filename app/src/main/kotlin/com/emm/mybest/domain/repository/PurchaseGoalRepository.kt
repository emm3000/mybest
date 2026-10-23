package com.emm.mybest.domain.repository

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.PurchaseGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Stores the single purchase goal and the real contributions made toward it.
 */
interface PurchaseGoalRepository {
    /**
     * Emits the purchase goal, or null when none is set.
     */
    fun observeGoal(): Flow<PurchaseGoal?>

    /**
     * Sets [goal] as the purchase goal, replacing any earlier one and keeping its contributions.
     */
    suspend fun saveGoal(goal: PurchaseGoal)

    /**
     * Removes the purchase goal and every contribution to it.
     */
    suspend fun clearGoal()

    /**
     * Emits every contribution, oldest app day first.
     */
    fun observeContributions(): Flow<List<GoalContribution>>

    /**
     * Records a real contribution of [amount] on [appDay].
     */
    suspend fun addContribution(appDay: LocalDate, amount: Money)

    /**
     * Removes the contribution with [id], if any.
     */
    suspend fun deleteContribution(id: Long)
}
