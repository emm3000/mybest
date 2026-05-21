package com.emm.mybest.features.insights.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.domain.models.InsightsRecommendationAction
import com.emm.mybest.domain.models.WeightEntry

@Stable
data class InsightsState(
    val weightHistory: List<WeightEntry> = emptyList(),
    val periodLabel: String = "",
    val totalWeightLost: Float = 0f,
    val currentWeight: Float = 0f,
    val initialWeight: Float = 0f,
    val deltaWeightKg: Float? = null,
    val deltaWeightPercent: Float? = null,
    val kgPerDayRate14d: Float? = null,
    val photoCount: Int = 0,
    val recommendationTitle: String = "",
    val recommendationDescription: String = "",
    val recommendationActionLabel: String = "",
    val recommendationAction: InsightsRecommendationAction? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val canComparePhotos: Boolean
        get() = photoCount >= MIN_COMPARE_PHOTOS

    val hasRecommendation: Boolean
        get() = recommendationAction != null

    private companion object {
        private const val MIN_COMPARE_PHOTOS = 2
    }
}
