package com.emm.mybest.features.debts.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.KeyboardType
import com.emm.mybest.R
import com.emm.mybest.domain.validation.DebtValidationError

@get:StringRes
internal val DebtFormField.labelRes: Int
    get() = when (this) {
        DebtFormField.NAME -> R.string.debts_field_name
        DebtFormField.BALANCE -> R.string.debts_field_balance
        DebtFormField.INSTALLMENT -> R.string.debts_field_installment
        DebtFormField.PAYMENT_DAY -> R.string.debts_field_payment_day
    }

internal val DebtFormField.keyboardType: KeyboardType
    get() = when (this) {
        DebtFormField.NAME -> KeyboardType.Text
        DebtFormField.BALANCE, DebtFormField.INSTALLMENT -> KeyboardType.Decimal
        DebtFormField.PAYMENT_DAY -> KeyboardType.Number
    }

internal fun DebtFormState.valueOf(field: DebtFormField): String = when (field) {
    DebtFormField.NAME -> name
    DebtFormField.BALANCE -> balance
    DebtFormField.INSTALLMENT -> installment
    DebtFormField.PAYMENT_DAY -> paymentDay
}

internal fun DebtFormState.errorFor(field: DebtFormField): DebtValidationError? =
    errors.firstOrNull { it.field() == field }

@StringRes
internal fun DebtValidationError.messageRes(): Int = when (this) {
    DebtValidationError.BlankName -> R.string.debts_error_blank_name
    DebtValidationError.MissingBalance -> R.string.debts_error_missing_balance
    DebtValidationError.NegativeBalance -> R.string.debts_error_negative_balance
    DebtValidationError.NonPositiveInstallment -> R.string.debts_error_installment
    DebtValidationError.PaymentDayOutOfRange -> R.string.debts_error_payment_day
}

private fun DebtValidationError.field(): DebtFormField = when (this) {
    DebtValidationError.BlankName -> DebtFormField.NAME
    DebtValidationError.MissingBalance, DebtValidationError.NegativeBalance -> DebtFormField.BALANCE
    DebtValidationError.NonPositiveInstallment -> DebtFormField.INSTALLMENT
    DebtValidationError.PaymentDayOutOfRange -> DebtFormField.PAYMENT_DAY
}
