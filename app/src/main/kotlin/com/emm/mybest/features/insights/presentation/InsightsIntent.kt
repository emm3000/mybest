package com.emm.mybest.features.insights.presentation

sealed class InsightsIntent {
    object OnHistoryClick : InsightsIntent()
    object OnAddWeightClick : InsightsIntent()
}
