package com.emm.mybest.features.preouting.presentation

sealed interface PreOutingEffect {
    data object OpenSettings : PreOutingEffect
    data object DayAlreadyDecided : PreOutingEffect
}
