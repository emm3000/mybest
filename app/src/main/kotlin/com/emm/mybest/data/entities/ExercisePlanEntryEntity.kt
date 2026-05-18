package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_plan_entries")
data class ExercisePlanEntryEntity(
    @PrimaryKey @ColumnInfo(name = "day_of_week") val dayOfWeek: String,
    @ColumnInfo(name = "routine") val routine: String,
)
