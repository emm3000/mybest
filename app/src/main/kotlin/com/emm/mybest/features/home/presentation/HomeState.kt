package com.emm.mybest.features.home.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.domain.models.PhotoType
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
    val weekNumber: Int = 0,
    val lastWeightKg: Float? = null,
    val previousWeightKg: Float? = null,
    val photoCount: Int = 0,
    val lastPhotoType: PhotoType? = null,
    val lastPhotoDaysAgo: Int? = null,
    val checks: List<DailyCheckItem> = emptyList(),
)
