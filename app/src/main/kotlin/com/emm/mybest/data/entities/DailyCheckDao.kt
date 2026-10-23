package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface DailyCheckDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(check: DailyCheckEntity)

    @Query(
        """
        SELECT * FROM daily_check
        WHERE app_day BETWEEN :from AND :to
        ORDER BY app_day ASC, type ASC
    """,
    )
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<DailyCheckEntity>>
}
