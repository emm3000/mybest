package com.emm.mybest.domain.models

data class InsightsData(
    val weightEntries: List<WeightEntry>,
    val period: PeriodLabel,
    val totalWeightLost: Float,
    val currentWeight: Float,
    val initialWeight: Float,
    val photoCount: Int,
    val recommendation: InsightsRecommendation,
    val deltaWeightKg: Float? = null,
    val deltaWeightPercent: Float? = null,
    val kgPerDayRate14d: Float? = null,
    val daysSinceFirstWeight: Int? = null,
    val troncoPhotoCount: Int = 0,
    val caraPhotoCount: Int = 0,
)
