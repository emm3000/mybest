package com.emm.mybest.features.insights.presentation

sealed class InsightsIntent {
    object OnCompareClick : InsightsIntent()
    object OnRecommendationActionClick : InsightsIntent()
    object OnHistoryClick : InsightsIntent()
    object OnAddWeightClick : InsightsIntent()
}
