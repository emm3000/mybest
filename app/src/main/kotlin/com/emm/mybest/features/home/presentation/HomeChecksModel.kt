package com.emm.mybest.features.home.presentation

data class HomeChecksModel(
    val items: List<DailyCheckItem>,
    val keptChecks: Int,
    val totalChecks: Int,
)
