package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface WonNightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(wonNight: WonNightEntity)

    @Query("DELETE FROM won_night WHERE app_day = :appDay")
    suspend fun deleteByAppDay(appDay: LocalDate)

    @Query(
        """
        SELECT * FROM won_night
        WHERE app_day BETWEEN :from AND :to
        ORDER BY app_day ASC
    """,
    )
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<WonNightEntity>>
}
