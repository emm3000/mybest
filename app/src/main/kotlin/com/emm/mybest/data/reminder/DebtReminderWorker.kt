package com.emm.mybest.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emm.mybest.domain.usecase.reminder.GetInstallmentRemindersUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DebtReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val getInstallmentReminders: GetInstallmentRemindersUseCase by inject()
    private val reminderNotifier: ReminderNotifier by inject()

    override suspend fun doWork(): Result {
        getInstallmentReminders().forEach { reminder -> reminderNotifier.show(reminder.toReminderNotification()) }
        return Result.success()
    }
}
