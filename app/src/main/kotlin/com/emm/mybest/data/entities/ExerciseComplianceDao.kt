package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ExerciseComplianceDao {

    @Query("SELECT * FROM exercise_compliance WHERE date = :date")
    fun observeByDate(date: LocalDate): Flow<ExerciseComplianceEntity?>

    @Upsert
    suspend fun upsert(entry: ExerciseComplianceEntity)

    @Query("SELECT * FROM exercise_compliance WHERE date BETWEEN :from AND :to")
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<ExerciseComplianceEntity>>
}
