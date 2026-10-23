package com.emm.mybest.features.checkin.presentation

import kotlinx.datetime.LocalTime

internal fun CheckInForm.reduce(intent: CheckInIntent): CheckInForm = when (intent) {
    is CheckInIntent.OnSpentChange -> copy(spentInput = intent.value)
    is CheckInIntent.OnDrinksChange -> copy(drinksInput = intent.value.filter(Char::isDigit))
    CheckInIntent.OnReturnTimeClick -> copy(isTimePickerOpen = true)
    CheckInIntent.OnReturnTimeDismiss -> copy(isTimePickerOpen = false)
    is CheckInIntent.OnReturnTimeSelected -> copy(
        returnTime = LocalTime(intent.hour, intent.minute),
        isTimePickerOpen = false,
    )
    is CheckInIntent.OnMoodSelect -> copy(mood = intent.mood)
    else -> this
}
