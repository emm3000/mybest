package com.emm.mybest.data.reminder

data class ReminderNotification(
    val channelId: String,
    val notificationId: Int,
    val content: ReminderContent,
)
