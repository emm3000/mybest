package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyWeightDao {

    @Query("""
        SELECT * FROM daily_weight
        WHERE date = :date
        LIMIT 1
    """)
    suspend fun getByDate(date: LocalDate): DailyWeightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(weight: DailyWeightEntity)

    @Query("""
        SELECT * FROM daily_weight
        ORDER BY date ASC
    """)
    fun observeAllOrdered(): Flow<List<DailyWeightEntity>>

    @Query("""
        SELECT * FROM daily_weight
        ORDER BY date DESC
        LIMIT 1
    """)
    suspend fun getLatest(): DailyWeightEntity?

    @Query("DELETE FROM daily_weight WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)
}
