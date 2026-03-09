package com.emm.mybest.domain.models

data class InsightsData(
    val weightEntries: List<WeightEntry>,
    val habitConsistency: Float,
    val totalWeightLost: Float,
    val currentWeight: Float,
    val initialWeight: Float,
    val exerciseDays: Int,
    val healthyEatingDays: Int,
)
