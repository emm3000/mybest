package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {

    @Query("SELECT * FROM meal_plan_entries")
    fun observeAll(): Flow<List<MealPlanEntryEntity>>

    @Upsert
    suspend fun upsert(entry: MealPlanEntryEntity)

    @Query("DELETE FROM meal_plan_entries")
    suspend fun clear()
}
