package com.emm.mybest.features.preouting.presentation

import kotlinx.datetime.LocalTime

internal fun PreOutingForm.reduce(intent: PreOutingIntent): PreOutingForm = when (intent) {
    PreOutingIntent.OnGoOutClick -> copy(isOpen = true)
    PreOutingIntent.OnFormDismiss -> PreOutingForm()
    PreOutingIntent.OnReturnTimeClick -> copy(isTimePickerOpen = true)
    PreOutingIntent.OnReturnTimeDismiss -> copy(isTimePickerOpen = false)
    is PreOutingIntent.OnReturnTimeSelected -> copy(
        returnTime = LocalTime(intent.hour, intent.minute),
        isTimePickerOpen = false,
    )
    else -> edit(intent)
}

private fun PreOutingForm.edit(intent: PreOutingIntent): PreOutingForm = when (intent) {
    is PreOutingIntent.OnMoneyChange -> copy(moneyInput = intent.value)
    is PreOutingIntent.OnDrinksChange -> copy(drinksInput = intent.value.filter(Char::isDigit))
    PreOutingIntent.OnCardsOffToggle -> copy(areCardsOff = !areCardsOff)
    PreOutingIntent.OnAccountLoadedToggle -> copy(isAccountLoaded = !isAccountLoaded)
    else -> this
}
