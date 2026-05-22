package com.emm.mybest.features.insights.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.domain.models.WeightEntry
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
data class InsightsState(
    val weightHistory: ImmutableList<WeightEntry> = persistentListOf(),
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
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val daysSinceFirstWeight: Int? = null,
    val troncoPhotoCount: Int = 0,
    val caraPhotoCount: Int = 0,
) {
    val hasRecommendation: Boolean
        get() = recommendationTitle.isNotEmpty()
}
