package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailySlot

sealed interface HomeIntent {
    data class ToggleSlot(val slot: DailySlot, val done: Boolean) : HomeIntent
    data class StartEditMeal(val slot: DailySlot) : HomeIntent
    data class UpdateMealDraft(val description: String) : HomeIntent
    data object SaveMealDraft : HomeIntent
    data object CancelEditMeal : HomeIntent
}
