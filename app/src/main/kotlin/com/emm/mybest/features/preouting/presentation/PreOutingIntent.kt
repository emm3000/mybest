package com.emm.mybest.features.preouting.presentation

sealed interface PreOutingIntent {
    data class OnPromptClick(val prompt: PreOutingPrompt) : PreOutingIntent
}
