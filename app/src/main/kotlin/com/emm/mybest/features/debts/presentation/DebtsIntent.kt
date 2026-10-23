package com.emm.mybest.features.debts.presentation

sealed interface DebtsIntent {
    sealed interface FormIntent : DebtsIntent
    sealed interface DeleteIntent : DebtsIntent

    data object OnAddClick : FormIntent
    data class OnEditClick(val id: Long) : FormIntent
    data class OnFormChange(val field: DebtFormField, val value: String) : FormIntent
    data object OnFormSave : FormIntent
    data object OnFormDismiss : FormIntent
    data class OnPayClick(val id: Long) : DebtsIntent
    data class OnDeleteClick(val id: Long) : DeleteIntent
    data object OnDeleteConfirm : DeleteIntent
    data object OnDeleteDismiss : DeleteIntent
}
