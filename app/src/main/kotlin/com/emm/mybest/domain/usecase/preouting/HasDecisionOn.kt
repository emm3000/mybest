package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

internal suspend fun OutingRepository.hasDecisionOn(appDay: LocalDate): Boolean =
    observeRange(appDay, appDay).first().isNotEmpty() || observeWonNights(appDay, appDay).first().isNotEmpty()
