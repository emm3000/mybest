package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.models.DailyCheckType

data class CheckDetailState(
    val type: DailyCheckType,
    val answer: CheckAnswer,
)
