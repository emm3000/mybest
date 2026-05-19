package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface DailyWeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(weight: DailyWeightEntity)

    @Query(
        """
        SELECT * FROM daily_weight
        ORDER BY date ASC
    """,
    )
    fun observeAllOrdered(): Flow<List<DailyWeightEntity>>

    @Query("DELETE FROM daily_weight WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)
}
