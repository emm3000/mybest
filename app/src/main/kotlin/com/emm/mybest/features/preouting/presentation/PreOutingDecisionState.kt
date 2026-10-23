package com.emm.mybest.features.preouting.presentation

sealed interface PreOutingDecisionState {
    data object Undecided : PreOutingDecisionState
    data class WentOut(
        val moneyLabel: String?,
        val drinksLabel: String?,
        val returnTimeLabel: String?,
    ) : PreOutingDecisionState
    data class StayedIn(val estimateLabel: String?) : PreOutingDecisionState
}
