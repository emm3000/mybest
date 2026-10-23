package com.emm.mybest.domain.validation

sealed interface PurchaseGoalValidationError {
    data object BlankName : PurchaseGoalValidationError
    data object NonPositivePrice : PurchaseGoalValidationError
}
