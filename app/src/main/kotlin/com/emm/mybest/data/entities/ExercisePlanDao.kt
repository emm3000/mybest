package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExercisePlanDao {

    @Query("SELECT * FROM exercise_plan_entries")
    fun observeAll(): Flow<List<ExercisePlanEntryEntity>>

    @Upsert
    suspend fun upsert(entry: ExercisePlanEntryEntity)

    @Query("DELETE FROM exercise_plan_entries")
    suspend fun clear()
}
