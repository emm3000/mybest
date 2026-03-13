package com.emm.mybest.domain.models

enum class InsightsRecommendationAction {
    PRIORITIZE_HABIT,
    ADJUST_WEIGHT_PLAN,
    ADD_PROGRESS_PHOTO,
    KEEP_ROUTINE,
}

data class InsightsRecommendation(
    val title: String,
    val description: String,
    val actionLabel: String,
    val action: InsightsRecommendationAction,
)

data class InsightsData(
    val weightEntries: List<WeightEntry>,
    val periodLabel: String,
    val habitConsistency: Float,
    val totalWeightLost: Float,
    val currentWeight: Float,
    val initialWeight: Float,
    val exerciseDays: Int,
    val healthyEatingDays: Int,
    val photoCount: Int,
    val recommendation: InsightsRecommendation,
)
