package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "meal_compliance",
    primaryKeys = ["date", "meal_type"],
)
data class MealComplianceEntity(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "meal_type") val mealType: String,
    @ColumnInfo(name = "done") val done: Boolean,
)
