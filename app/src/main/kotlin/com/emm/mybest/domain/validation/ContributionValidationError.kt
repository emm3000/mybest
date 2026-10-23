package com.emm.mybest.domain.validation

sealed interface ContributionValidationError {
    data object NonPositiveAmount : ContributionValidationError
}
