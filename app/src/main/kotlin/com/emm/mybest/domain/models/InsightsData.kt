package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

enum class InsightsRecommendationAction {
    ADJUST_WEIGHT_PLAN,
    ADD_PROGRESS_PHOTO,
    KEEP_ROUTINE,
}

enum class InsightsRecommendationKind {
    ADJUST_WEEKLY_PLAN,
    UPLOAD_PHOTO_TODAY,
    KEEP_ROUTINE,
}

data class InsightsRecommendation(
    val kind: InsightsRecommendationKind,
    val action: InsightsRecommendationAction,
)

sealed interface PeriodLabel {
    object NoData : PeriodLabel
    data class SingleDay(val date: LocalDate) : PeriodLabel
    data class Range(val start: LocalDate, val end: LocalDate) : PeriodLabel
}

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
)
