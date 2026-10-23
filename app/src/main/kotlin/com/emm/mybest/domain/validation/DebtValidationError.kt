package com.emm.mybest.domain.validation

sealed interface DebtValidationError {
    data object BlankName : DebtValidationError
    data object MissingBalance : DebtValidationError
    data object NegativeBalance : DebtValidationError
    data object NonPositiveInstallment : DebtValidationError
    data object PaymentDayOutOfRange : DebtValidationError
}
