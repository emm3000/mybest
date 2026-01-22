package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "daily_habit"
)
data class DailyHabitEntity(

    @PrimaryKey
    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "ate_healthy")
    val ateHealthy: Boolean = false,

    @ColumnInfo(name = "did_exercise")
    val didExercise: Boolean = false,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    // Extensión futura (NO usar aún)
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
