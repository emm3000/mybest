package com.emm.mybest.features.debts.presentation

data class DebtsState(
    val isLoaded: Boolean = false,
    val debts: List<DebtItemState> = emptyList(),
    val form: DebtFormState? = null,
    val pendingDelete: DebtItemState? = null,
)
