package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class HabitType {
    BOOLEAN, // Yes/No
    TIME, // e.g., Read 30 mins
    METRIC // e.g., Drink 2L water
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon")
    val icon: String, // Resource name or emoji

    @ColumnInfo(name = "color")
    val color: Int, // ARGB

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "type")
    val type: HabitType,

    @ColumnInfo(name = "goal_value")
    val goalValue: Float? = null, // e.g., 30.0 for 30 mins, 2.0 for 2 liters

    @ColumnInfo(name = "unit")
    val unit: String? = null, // e.g., "min", "litros", "kg"

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    // Frequency as a simple JSON string or comma-separated days for now
    // Example: "1,2,3,4,5,6,7" for every day, "1,3,5" for Mon/Wed/Fri
    @ColumnInfo(name = "scheduled_days")
    val scheduledDays: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
