package com.emm.mybest.data.reminder

import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

internal fun reminderTimeTag(time: LocalTime, timeZone: TimeZone): String = "reminder-time:$time@${timeZone.id}"

internal suspend fun WorkManager.isReminderScheduledAt(workName: String, time: LocalTime, timeZone: TimeZone): Boolean =
    getWorkInfosForUniqueWorkFlow(workName).first().any { info ->
        !info.state.isFinished && reminderTimeTag(time, timeZone) in info.tags
    }
