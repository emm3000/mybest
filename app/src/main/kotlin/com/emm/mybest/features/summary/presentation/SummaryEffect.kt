package com.emm.mybest.features.summary.presentation

sealed interface SummaryEffect {
    data object OpenSettings : SummaryEffect
}
