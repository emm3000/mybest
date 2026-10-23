package com.emm.mybest.features.summary.presentation

sealed interface SummaryIntent {
    data object OnPreviousWeekClick : SummaryIntent
    data object OnNextWeekClick : SummaryIntent
    data class OnSetupPromptClick(val destination: SummaryDestination) : SummaryIntent
}
