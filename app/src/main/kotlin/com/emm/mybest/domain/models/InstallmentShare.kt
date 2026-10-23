package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalDate

/**
 * The next installment to pay: [installment] of the debt [debtName] due on [paymentDate], and [nightPercent], the
 * whole percentage of it that one estimated outing represents, or null when there is no estimate.
 */
data class InstallmentShare(
    val debtName: String,
    val paymentDate: LocalDate,
    val installment: Money,
    val nightPercent: Int?,
)
