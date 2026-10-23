package com.emm.mybest.features.goal.presentation

import com.emm.mybest.domain.validation.ContributionValidationError

data class ContributionFormState(
    val amount: String = "",
    val error: ContributionValidationError? = null,
)
