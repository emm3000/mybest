package com.emm.mybest.features.preouting.presentation

sealed interface PreOutingEffect {
    data object OpenSettings : PreOutingEffect
    data object OpenDebts : PreOutingEffect
    data object OpenPurchaseGoal : PreOutingEffect
    data object DayAlreadyDecided : PreOutingEffect
    data object NotWonNightDay : PreOutingEffect
}
