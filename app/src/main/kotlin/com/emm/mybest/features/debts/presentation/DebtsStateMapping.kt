package com.emm.mybest.features.debts.presentation

import com.emm.mybest.core.money.Money
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtListItem
import com.emm.mybest.domain.validation.DebtDraft

private const val NEGATIVE_SIGN = "-"

internal fun DebtListItem.toItemState(): DebtItemState = DebtItemState(
    id = debt.id,
    name = debt.name,
    balanceLabel = debt.balance.formatPen(),
    installmentLabel = debt.installment.formatPen(),
    paymentDay = debt.paymentDay,
    isDueInstallmentPaid = isDueInstallmentPaid,
    isPaidOff = !debt.hasBalance,
)

internal fun Debt.toFormState(): DebtFormState = DebtFormState(
    id = id,
    name = name,
    balance = balance.toInputText(),
    installment = installment.toInputText(),
    paymentDay = paymentDay.toString(),
)

internal fun DebtFormState.toDraft(): DebtDraft = DebtDraft(
    name = name,
    balance = parseBalance(balance),
    installment = Money.parseOrNull(installment),
    paymentDay = paymentDay.trim().toIntOrNull(),
)

internal fun DebtFormState.withValue(field: DebtFormField, value: String): DebtFormState {
    val edited = when (field) {
        DebtFormField.NAME -> copy(name = value)
        DebtFormField.BALANCE -> copy(balance = value)
        DebtFormField.INSTALLMENT -> copy(installment = value)
        DebtFormField.PAYMENT_DAY -> copy(paymentDay = value)
    }
    return edited.copy(errors = emptySet())
}

private fun Money.toInputText(): String {
    val soles = cents / Money.CENTS_PER_SOL
    val remainder = cents % Money.CENTS_PER_SOL
    return if (remainder == 0L) soles.toString() else "%d.%02d".format(soles, remainder)
}

private fun parseBalance(input: String): Money? {
    val trimmed = input.trim()
    if (!trimmed.startsWith(NEGATIVE_SIGN)) return Money.parseOrNull(trimmed)
    return Money.parseOrNull(trimmed.removePrefix(NEGATIVE_SIGN))?.let { Money(-it.cents) }
}
