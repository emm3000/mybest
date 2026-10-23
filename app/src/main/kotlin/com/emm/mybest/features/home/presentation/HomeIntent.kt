package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailyCheckType

sealed interface HomeIntent {
    data class ToggleDailyCheck(val type: DailyCheckType) : HomeIntent

    data class AnswerAlcohol(val hasDrunk: Boolean) : HomeIntent

    data object DefineWeeklyTarget : HomeIntent

    data object ShowDailyChecks : HomeIntent

    data object Resume : HomeIntent
}
