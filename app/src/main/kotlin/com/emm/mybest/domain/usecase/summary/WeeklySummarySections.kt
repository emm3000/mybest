package com.emm.mybest.domain.usecase.summary

/**
 * The section use cases the weekly summary is composed from, each owning the rules of one part of the summary.
 */
data class WeeklySummarySections(
    val outings: ObserveWeekOutingsUseCase,
)
