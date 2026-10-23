package com.emm.mybest.features.preouting.presentation

sealed interface PreOutingIntent {
    data class OnPromptClick(val prompt: PreOutingPrompt) : PreOutingIntent
    data object OnStayInClick : PreOutingIntent
    data object OnGoOutClick : PreOutingIntent
    data object OnFormDismiss : PreOutingIntent
    data class OnMoneyChange(val value: String) : PreOutingIntent
    data class OnDrinksChange(val value: String) : PreOutingIntent
    data object OnReturnTimeClick : PreOutingIntent
    data object OnReturnTimeDismiss : PreOutingIntent
    data class OnReturnTimeSelected(val hour: Int, val minute: Int) : PreOutingIntent
    data object OnCardsOffToggle : PreOutingIntent
    data object OnAccountLoadedToggle : PreOutingIntent
    data object OnConfirmOutingClick : PreOutingIntent
}
