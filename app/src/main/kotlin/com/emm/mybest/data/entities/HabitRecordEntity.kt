package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

/**
 * Tracks the daily completion or value for a specific habit.
 * Each habit can have multiple records (one per date).
 */
@Entity(
    tableName = "habit_records",
    indices = [
        Index(value = ["habit_id", "date"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HabitRecordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "habit_id")
    val habitId: String,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "value")
    val value: Float, // 1.0 for boolean true, or numeric value in minutes/meters/liters

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false, // Boolean flag for convenience

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
