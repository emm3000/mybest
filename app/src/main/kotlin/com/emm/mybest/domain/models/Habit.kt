package com.emm.mybest.domain.models

import java.time.DayOfWeek

enum class HabitType {
    BOOLEAN,
    TIME,
    METRIC
}

data class Habit(
    val id: String,
    val name: String,
    val icon: String,
    val color: Int,
    val category: String,
    val type: HabitType,
    val goalValue: Float? = null,
    val unit: String? = null,
    val isEnabled: Boolean = true,
    val scheduledDays: Set<DayOfWeek>,
    val createdAt: Long = System.currentTimeMillis()
)
