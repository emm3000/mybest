package com.emm.mybest.domain.models

data class InsightsRecommendation(
    val title: String,
    val description: String,
    val actionLabel: String,
)

data class InsightsData(
    val weightEntries: List<WeightEntry>,
    val habitConsistency: Float,
    val totalWeightLost: Float,
    val currentWeight: Float,
    val initialWeight: Float,
    val exerciseDays: Int,
    val healthyEatingDays: Int,
    val photoCount: Int,
    val recommendation: InsightsRecommendation,
)
