package com.emm.mybest.features.weight.presentation

import com.emm.mybest.core.datetime.longEs
import com.emm.mybest.domain.models.Setup
import java.util.Locale

internal fun AddWeightState.withSetup(setup: Setup): AddWeightState = copy(
    targetWeightKg = setup.targetWeightKg,
    weighInDay = setup.weighInDay,
    weighInDayLabel = setup.weighInDay?.longEs()?.lowercase(Locale.ROOT),
)
