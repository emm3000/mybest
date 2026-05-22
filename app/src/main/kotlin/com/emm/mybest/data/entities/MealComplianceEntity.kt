package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "meal_compliance",
    primaryKeys = ["date", "meal_type"],
)
data class MealComplianceEntity(
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "meal_type") val mealType: String,
    @ColumnInfo(name = "done") val done: Boolean,
)
