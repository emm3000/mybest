package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

/**
 * The installment of [debt] that falls due on [dueDate] and still needs a reminder.
 */
data class InstallmentReminder(
    val debt: Debt,
    val dueDate: LocalDate,
)
