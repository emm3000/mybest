package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PurchaseGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(goal: PurchaseGoalEntity)

    @Query("SELECT * FROM purchase_goal WHERE id = ${PurchaseGoalEntity.SINGLE_ROW_ID}")
    abstract fun observe(): Flow<PurchaseGoalEntity?>

    @Insert
    abstract suspend fun insertContribution(contribution: GoalContributionEntity): Long

    @Query("DELETE FROM goal_contribution WHERE id = :id")
    abstract suspend fun deleteContribution(id: Long)

    @Query("SELECT * FROM goal_contribution ORDER BY app_day ASC, id ASC")
    abstract fun observeContributions(): Flow<List<GoalContributionEntity>>

    /**
     * Deletes the purchase goal and every contribution to it in one transaction.
     */
    @Transaction
    open suspend fun clear() {
        deleteGoal()
        deleteAllContributions()
    }

    @Query("DELETE FROM purchase_goal")
    protected abstract suspend fun deleteGoal()

    @Query("DELETE FROM goal_contribution")
    protected abstract suspend fun deleteAllContributions()
}
