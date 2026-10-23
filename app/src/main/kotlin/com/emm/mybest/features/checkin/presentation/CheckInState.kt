package com.emm.mybest.features.checkin.presentation

import com.emm.mybest.ui.components.LimitOutcomeState

data class CheckInState(
    val isLoaded: Boolean = false,
    val step: CheckInStep = CheckInStep.NO_TASK,
    val dayLabel: String = "",
    val limits: CheckInLimitsState? = null,
    val form: CheckInForm = CheckInForm(),
    val returnTimeLabel: String? = null,
    val canSave: Boolean = false,
    val outcomes: List<LimitOutcomeState> = emptyList(),
)
