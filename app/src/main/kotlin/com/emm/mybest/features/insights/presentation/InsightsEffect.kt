package com.emm.mybest.features.insights.presentation

import com.emm.mybest.domain.models.InsightsRecommendationAction

sealed class InsightsEffect {
    object NavigateToCompare : InsightsEffect()
    data class NavigateByRecommendation(val action: InsightsRecommendationAction) : InsightsEffect()
    object NavigateToHistory : InsightsEffect()
    object NavigateToAddWeight : InsightsEffect()
}
