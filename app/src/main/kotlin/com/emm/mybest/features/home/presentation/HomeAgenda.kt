package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.InstallmentShare
import com.emm.mybest.domain.usecase.home.HomeDestination

internal data class HomeAgenda(
    val destination: HomeDestination,
    val hasPendingCheckIn: Boolean,
    val nextInstallment: InstallmentShare?,
)
