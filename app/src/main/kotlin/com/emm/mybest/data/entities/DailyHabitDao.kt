package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyHabitDao {

    @Query("""
        SELECT * FROM daily_habit
        WHERE date = :date
        LIMIT 1
    """)
    suspend fun getByDate(date: LocalDate): DailyHabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: DailyHabitEntity)

    @Query("""
        SELECT * FROM daily_habit
        WHERE date = :date
        LIMIT 1
    """)
    fun observeByDate(date: LocalDate): Flow<DailyHabitEntity?>

    @Query("""
        SELECT * FROM daily_habit
        ORDER BY date DESC
    """)
    fun observeAll(): Flow<List<DailyHabitEntity>>

    @Query("""
        SELECT COUNT(*) FROM daily_habit
        WHERE did_exercise = 1
    """)
    suspend fun countExerciseDays(): Int

    @Query("DELETE FROM daily_habit WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)
}
