package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "meal_plan_entries",
    primaryKeys = ["day_of_week", "meal_type"],
)
data class MealPlanEntryEntity(
    @ColumnInfo(name = "day_of_week") val dayOfWeek: String,
    @ColumnInfo(name = "meal_type") val mealType: String,
    @ColumnInfo(name = "description") val description: String,
)
