package com.emm.mybest.features.settings.presentation

import kotlinx.datetime.LocalTime

internal fun formatReminderTime(time: LocalTime?): String =
    if (time != null) {
        "%02d:%02d".format(time.hour, time.minute)
    } else {
        "—"
    }
