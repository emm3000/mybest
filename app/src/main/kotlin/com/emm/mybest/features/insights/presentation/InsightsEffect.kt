package com.emm.mybest.features.insights.presentation

sealed class InsightsEffect {
    object NavigateToHistory : InsightsEffect()
    object NavigateToAddWeight : InsightsEffect()
}
