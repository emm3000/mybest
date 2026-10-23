package com.emm.mybest.features.summary.presentation

data class SummaryState(
    val weekLabel: String = "",
    val canGoNext: Boolean = false,
    val sections: List<SummarySectionState> = emptyList(),
)
