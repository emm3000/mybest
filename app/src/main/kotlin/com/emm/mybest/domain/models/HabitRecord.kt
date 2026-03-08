package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

data class HabitRecord(
    val id: String,
    val habitId: String,
    val date: LocalDate,
    val value: Float,
    val isCompleted: Boolean,
    val notes: String? = null
)

data class HabitWithRecord(
    val habit: Habit,
    val record: HabitRecord?
)
