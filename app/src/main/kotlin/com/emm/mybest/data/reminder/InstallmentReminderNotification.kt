package com.emm.mybest.data.reminder

import com.emm.mybest.R
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.core.navigation.ACTION_DEBTS
import com.emm.mybest.core.notification.DEBT_REMINDER_CHANNEL_ID
import com.emm.mybest.domain.models.InstallmentReminder

private const val INSTALLMENT_NOTIFICATION_ID_BASE = 2000

internal fun InstallmentReminder.toReminderNotification(): ReminderNotification = ReminderNotification(
    channelId = DEBT_REMINDER_CHANNEL_ID,
    notificationId = INSTALLMENT_NOTIFICATION_ID_BASE + debt.id.toInt(),
    content = ReminderContent(
        action = ACTION_DEBTS,
        titleRes = R.string.debt_reminder_title,
        text = ReminderText { context ->
            context.getString(R.string.debt_reminder_text, debt.name, debt.nextInstallment.formatPen(), dueDate.day)
        },
    ),
)
