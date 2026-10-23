package com.emm.mybest.features.checkin.presentation

sealed interface CheckInEffect {
    data object Close : CheckInEffect
    data object EveningAlreadyRecorded : CheckInEffect
    data object InvalidInput : CheckInEffect
    data object ActionFailed : CheckInEffect
}
