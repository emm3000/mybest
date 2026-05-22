package com.emm.mybest.features.home.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.features.diet.presentation.edit.EditingMealDraft
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

private const val DEFAULT_TOTAL = 5

@Stable
data class HomeState(
    val isLoading: Boolean = true,
    val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val dayOfWeek: DayOfWeek = Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek,
    val weekNumber: Int = 0,
    val planRows: ImmutableList<PlanRow> = persistentListOf(),
    val completionRatio: Float = 0f,
    val completedCount: Int = 0,
    val totalCount: Int = DEFAULT_TOTAL,
    val streakDays: Int = 0,
    val lastWeightKg: Float? = null,
    val previousWeightKg: Float? = null,
    val photoCount: Int = 0,
    val lastPhotoType: PhotoType? = null,
    val lastPhotoDaysAgo: Int? = null,
    val editingMeal: EditingMealDraft? = null,
)
