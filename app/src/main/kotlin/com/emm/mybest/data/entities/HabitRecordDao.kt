package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface HabitRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HabitRecordEntity)

    @Query("SELECT * FROM habit_records WHERE habit_id = :habitId AND date = :date")
    suspend fun getRecordForDate(habitId: String, date: LocalDate): HabitRecordEntity?

    @Query("SELECT * FROM habit_records WHERE date = :date")
    fun getRecordsByDate(date: LocalDate): Flow<List<HabitRecordEntity>>

    @Query("SELECT * FROM habit_records WHERE habit_id = :habitId ORDER BY date DESC")
    fun getRecordsForHabit(habitId: String): Flow<List<HabitRecordEntity>>

    @Query("DELETE FROM habit_records WHERE id = :id")
    suspend fun deleteById(id: String)
}
