package com.emm.mybest.features.summary.presentation

import androidx.annotation.StringRes

data class SummarySectionState(
    @StringRes val titleRes: Int,
    val rows: List<SummaryRowState>,
)
