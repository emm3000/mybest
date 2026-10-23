package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeekProgress
import com.emm.mybest.domain.models.WeightEntry
import kotlinx.datetime.LocalDate

internal data class HomeInputs(
    val today: LocalDate,
    val weights: List<WeightEntry>,
    val photos: List<ProgressPhoto>,
    val recordedChecks: List<DailyCheck>,
    val weekProgress: Map<DailyCheckType, WeekProgress>,
    val routing: HomeRouting,
)
