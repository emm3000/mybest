package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface MealComplianceDao {

    @Query("SELECT * FROM meal_compliance WHERE date = :date")
    fun observeByDate(date: LocalDate): Flow<List<MealComplianceEntity>>

    @Upsert
    suspend fun upsert(entry: MealComplianceEntity)

    @Query("SELECT * FROM meal_compliance WHERE date BETWEEN :from AND :to")
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<MealComplianceEntity>>
}
