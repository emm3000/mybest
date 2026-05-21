package com.emm.mybest.features.home.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.domain.models.MealType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Stable
data class HomeState(
    val isLoading: Boolean = true,
    val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val dayOfWeek: DayOfWeek = Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek,
    val mealRows: List<MealRow> = emptyList(),
    val exerciseRoutine: String = "",
    val exerciseDone: Boolean = false,
    val completionRatio: Float = 0f,
    val completedCount: Int = 0,
    val totalCount: Int = MealType.entries.size + 1,
    val streakDays: Int = 0,
)
