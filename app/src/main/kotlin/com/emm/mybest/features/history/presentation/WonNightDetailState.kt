package com.emm.mybest.features.history.presentation

sealed interface WonNightDetailState {
    data class Estimated(val amountLabel: String) : WonNightDetailState

    data object Unestimated : WonNightDetailState
}
