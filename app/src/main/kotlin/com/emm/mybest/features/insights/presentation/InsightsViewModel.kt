package com.emm.mybest.features.insights.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.formatEsLongDate
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.domain.models.InsightsRecommendationAction
import com.emm.mybest.domain.models.InsightsRecommendationKind
import com.emm.mybest.domain.models.PeriodLabel
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.GetInsightsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

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

sealed class InsightsIntent {
    object OnCompareClick : InsightsIntent()
    object OnRecommendationActionClick : InsightsIntent()
    object OnHistoryClick : InsightsIntent()
    object OnAddWeightClick : InsightsIntent()
}

sealed class InsightsEffect {
    object NavigateToCompare : InsightsEffect()
    data class NavigateByRecommendation(val action: InsightsRecommendationAction) : InsightsEffect()
    object NavigateToHistory : InsightsEffect()
    object NavigateToAddWeight : InsightsEffect()
}

class InsightsViewModel(
    getInsightsUseCase: GetInsightsUseCase,
    clock: Clock = Clock.System,
) : ViewModel() {

    private val _effect = MutableSharedFlow<InsightsEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST,
    )
    val effect = _effect.asSharedFlow()

    val state: StateFlow<InsightsState> = getInsightsUseCase(clock.todayIn(TimeZone.currentSystemDefault()))
        .map { data ->
            val (recTitle, recDesc, recLabel) = mapRecommendationStrings(data.recommendation.kind)
            InsightsState(
                weightHistory = data.weightEntries,
                periodLabel = mapPeriodLabel(data.period),
                totalWeightLost = data.totalWeightLost,
                currentWeight = data.currentWeight,
                initialWeight = data.initialWeight,
                deltaWeightKg = data.deltaWeightKg,
                deltaWeightPercent = data.deltaWeightPercent,
                kgPerDayRate14d = data.kgPerDayRate14d,
                photoCount = data.photoCount,
                recommendationTitle = recTitle,
                recommendationDescription = recDesc,
                recommendationActionLabel = recLabel,
                recommendationAction = data.recommendation.action,
                isLoading = false,
                errorMessage = null,
            )
        }.catch { throwable ->
            emit(
                InsightsState(
                    isLoading = false,
                    errorMessage = throwable.message ?: "No se pudo cargar la información de estadísticas.",
                ),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = InsightsState(isLoading = true),
        )

    fun onIntent(intent: InsightsIntent) {
        viewModelScope.launch {
            when (intent) {
                InsightsIntent.OnCompareClick -> _effect.emit(InsightsEffect.NavigateToCompare)
                InsightsIntent.OnRecommendationActionClick -> {
                    state.value.recommendationAction?.let { action ->
                        _effect.emit(InsightsEffect.NavigateByRecommendation(action))
                    }
                }
                InsightsIntent.OnHistoryClick -> _effect.emit(InsightsEffect.NavigateToHistory)
                InsightsIntent.OnAddWeightClick -> _effect.emit(InsightsEffect.NavigateToAddWeight)
            }
        }
    }
}

private fun mapPeriodLabel(period: PeriodLabel): String = when (period) {
    PeriodLabel.NoData -> "Sin periodo disponible aún."
    is PeriodLabel.SingleDay -> "Datos del ${period.date.formatEsLongDate()}"
    is PeriodLabel.Range -> "Datos del ${period.start.formatEsLongDate()} al ${period.end.formatEsLongDate()}"
}

private data class RecommendationStrings(
    val title: String,
    val description: String,
    val actionLabel: String,
)

private fun mapRecommendationStrings(kind: InsightsRecommendationKind): RecommendationStrings = when (kind) {
    InsightsRecommendationKind.ADJUST_WEEKLY_PLAN -> RecommendationStrings(
        title = "Ajusta tu plan semanal",
        description = "No hay mejora reciente de peso. Ajusta alimentación o entrenamiento 3 días esta semana.",
        actionLabel = "Define un ajuste concreto",
    )
    InsightsRecommendationKind.UPLOAD_PHOTO_TODAY -> RecommendationStrings(
        title = "Registra evidencia visual",
        description = "Añade al menos 2 fotos por semana para comparar cambios reales.",
        actionLabel = "Sube una foto hoy",
    )
    InsightsRecommendationKind.KEEP_ROUTINE -> RecommendationStrings(
        title = "Mantén el ritmo",
        description = "Tu progreso es consistente. Conserva tu rutina y registra evidencia cada semana.",
        actionLabel = "Sostén la rutina actual",
    )
}
