package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
abstract class OutingDao {

    /**
     * Inserts [outing] and returns its new id, or -1 when its app day already has an outing.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(outing: OutingEntity): Long

    /**
     * Inserts [outing] and deletes the won night on its app day in one transaction, and returns the new id.
     *
     * @throws IllegalStateException when the app day already has an outing; the won night is then kept.
     */
    @Transaction
    open suspend fun insertReplacingWonNight(outing: OutingEntity): Long {
        val id = insert(outing)
        check(id != IGNORED_INSERT_ID) { "App day ${outing.appDay} already has an outing" }
        deleteWonNightOn(outing.appDay)
        return id
    }

    @Update
    abstract suspend fun update(outing: OutingEntity)

    @Query("SELECT * FROM outing WHERE id = :id")
    abstract suspend fun findById(id: Long): OutingEntity?

    @Query("SELECT * FROM outing ORDER BY app_day ASC")
    abstract fun observeAll(): Flow<List<OutingEntity>>

    @Query(
        """
        SELECT * FROM outing
        WHERE app_day BETWEEN :from AND :to
        ORDER BY app_day ASC
    """,
    )
    abstract fun observeRange(from: LocalDate, to: LocalDate): Flow<List<OutingEntity>>

    @Query("SELECT * FROM outing WHERE spent_cents IS NULL ORDER BY app_day ASC")
    abstract fun observePending(): Flow<List<OutingEntity>>

    @Query("DELETE FROM won_night WHERE app_day = :appDay")
    protected abstract suspend fun deleteWonNightOn(appDay: LocalDate)

    companion object {
        const val IGNORED_INSERT_ID = -1L
    }
}
