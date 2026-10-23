package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.usecase.home.HomeDestination

internal data class HomeRouting(
    val destination: HomeDestination,
    val hasPendingCheckIn: Boolean,
)
