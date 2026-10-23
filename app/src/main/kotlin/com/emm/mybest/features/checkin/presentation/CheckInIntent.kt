package com.emm.mybest.features.checkin.presentation

sealed interface CheckInIntent {
    data class OnSpentChange(val value: String) : CheckInIntent
    data class OnDrinksChange(val value: String) : CheckInIntent
    data object OnReturnTimeClick : CheckInIntent
    data object OnReturnTimeDismiss : CheckInIntent
    data class OnReturnTimeSelected(val hour: Int, val minute: Int) : CheckInIntent
    data class OnMoodSelect(val mood: Int) : CheckInIntent
    data object OnSaveClick : CheckInIntent
    data object OnNotNowClick : CheckInIntent
    data object OnWentOutYesClick : CheckInIntent
    data object OnWentOutNoClick : CheckInIntent
    data object OnDoneClick : CheckInIntent
}
