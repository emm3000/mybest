package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MealComplianceDao {

    @Query("SELECT * FROM meal_compliance WHERE date = :date")
    fun observeByDate(date: String): Flow<List<MealComplianceEntity>>

    @Upsert
    suspend fun upsert(entry: MealComplianceEntity)

    @Query("SELECT * FROM meal_compliance WHERE date BETWEEN :from AND :to")
    fun observeRange(from: String, to: String): Flow<List<MealComplianceEntity>>
}
