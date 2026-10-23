package com.emm.mybest.features.summary.presentation

import androidx.compose.ui.graphics.Color

sealed interface SummaryRowState {
    data class Line(
        val label: SummaryText,
        val value: SummaryText,
        val valueColor: Color,
    ) : SummaryRowState

    data class SetupPrompt(val text: SummaryText, val destination: SummaryDestination) : SummaryRowState
}
