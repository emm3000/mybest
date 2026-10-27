package com.emm.mybest.domain.usecase.summary

import com.emm.mybest.domain.usecase.dailycheck.ObserveWeekProgressUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeighInTrendUseCase

/**
 * The section use cases the weekly summary is composed from, each owning the rules of one part of the summary.
 */
data class WeeklySummarySections(
    val outings: ObserveWeekOutingsUseCase,
    val relapses: ObserveWeekRelapsesUseCase,
    val weekProgress: ObserveWeekProgressUseCase,
    val weighInTrend: ObserveWeighInTrendUseCase,
    val money: ObserveWeekMoneyUseCase,
)
