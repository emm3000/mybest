package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailyCheckType

data class HomeCheckActions(
    val onToggle: (DailyCheckType) -> Unit,
    val onAnswerAlcohol: (Boolean) -> Unit,
)
