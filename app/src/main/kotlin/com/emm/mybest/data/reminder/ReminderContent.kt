package com.emm.mybest.data.reminder

import androidx.annotation.StringRes

data class ReminderContent(
    val action: String,
    @StringRes val titleRes: Int,
    @StringRes val textRes: Int,
)
