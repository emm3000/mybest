package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface OutingDao {

    /**
     * Inserts [outing] and returns its new id, or -1 when its app day already has an outing.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(outing: OutingEntity): Long

    @Update
    suspend fun update(outing: OutingEntity)

    @Query("SELECT * FROM outing WHERE id = :id")
    suspend fun findById(id: Long): OutingEntity?

    @Query("SELECT * FROM outing ORDER BY app_day ASC")
    fun observeAll(): Flow<List<OutingEntity>>

    @Query(
        """
        SELECT * FROM outing
        WHERE app_day BETWEEN :from AND :to
        ORDER BY app_day ASC
    """,
    )
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<OutingEntity>>

    @Query("SELECT * FROM outing WHERE spent_cents IS NULL ORDER BY app_day ASC")
    fun observePending(): Flow<List<OutingEntity>>
}
