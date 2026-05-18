package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseComplianceDao {

    @Query("SELECT * FROM exercise_compliance WHERE date = :date")
    fun observeByDate(date: String): Flow<ExerciseComplianceEntity?>

    @Upsert
    suspend fun upsert(entry: ExerciseComplianceEntity)

    @Query("SELECT * FROM exercise_compliance WHERE date BETWEEN :from AND :to")
    fun observeRange(from: String, to: String): Flow<List<ExerciseComplianceEntity>>
}
