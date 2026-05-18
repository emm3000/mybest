package com.emm.mybest.domain.reminder

import kotlinx.datetime.LocalTime

interface WeightReminderScheduler {
    suspend fun schedule(time: LocalTime)
    suspend fun cancel()
}
