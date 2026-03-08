package com.emm.mybest.domain.models

data class HomeSummary(
    val dailyHabits: List<HabitWithRecord>,
    val latestWeight: Float?,
    val totalWeightLost: Float,
    val totalPhotos: Int
)
