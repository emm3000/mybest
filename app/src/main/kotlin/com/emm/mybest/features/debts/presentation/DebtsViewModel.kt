package com.emm.mybest.features.debts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.validation.DebtValidationError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DebtsViewModel(
    private val useCases: DebtsUseCases,
) : ViewModel() {

    private val form = MutableStateFlow<DebtFormState?>(null)
    private val pendingDeleteId = MutableStateFlow<Long?>(null)

    private val _effect = effectFlow<DebtsEffect>()
    val effect: SharedFlow<DebtsEffect> = _effect.asSharedFlow()

    val state: StateFlow<DebtsState> = combine(
        useCases.observeDebtList(),
        form,
        pendingDeleteId,
    ) { items, form, deleteId ->
        val debts = items.map { it.toItemState() }
        DebtsState(isLoaded = true, debts = debts, form = form, pendingDelete = debts.firstOrNull { it.id == deleteId })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = DebtsState(),
    )

    fun onIntent(intent: DebtsIntent) {
        when (intent) {
            is DebtsIntent.FormIntent -> onFormIntent(intent)
            is DebtsIntent.DeleteIntent -> onDeleteIntent(intent)
            is DebtsIntent.OnPayClick -> pay(intent.id)
        }
    }

    private fun onFormIntent(intent: DebtsIntent.FormIntent) {
        when (intent) {
            is DebtsIntent.OnAddClick -> form.value = DebtFormState()
            is DebtsIntent.OnEditClick -> openEditForm(intent.id)
            is DebtsIntent.OnFormChange -> form.update { it?.withValue(intent.field, intent.value) }
            is DebtsIntent.OnFormSave -> saveForm()
            is DebtsIntent.OnFormDismiss -> form.value = null
        }
    }

    private fun onDeleteIntent(intent: DebtsIntent.DeleteIntent) {
        when (intent) {
            is DebtsIntent.OnDeleteClick -> pendingDeleteId.value = intent.id
            is DebtsIntent.OnDeleteConfirm -> confirmDelete()
            is DebtsIntent.OnDeleteDismiss -> pendingDeleteId.value = null
        }
    }

    private fun openEditForm(id: Long) {
        viewModelScope.launch {
            val debt = attempt { useCases.debtRepository.observeDebts().first().firstOrNull { it.id == id } }.getOrNull()
            if (debt == null) _effect.emit(DebtsEffect.ActionFailed) else form.value = debt.toFormState()
        }
    }

    private fun saveForm() {
        val current = form.value ?: return
        viewModelScope.launch {
            attempt { useCases.saveDebt(current.id, current.toDraft()) }
                .onSuccess { errors -> applySaveResult(current.id, errors.toSet()) }
                .onFailure { _effect.emit(DebtsEffect.ActionFailed) }
        }
    }

    private fun applySaveResult(savedId: Long, errors: Set<DebtValidationError>) {
        form.update { latest ->
            when {
                latest?.id != savedId -> latest
                errors.isEmpty() -> null
                else -> latest.copy(errors = errors)
            }
        }
    }

    private fun pay(id: Long) {
        viewModelScope.launch {
            attempt { useCases.markInstallmentPaid(id) }.onFailure { _effect.emit(DebtsEffect.ActionFailed) }
        }
    }

    private fun confirmDelete() {
        val id = pendingDeleteId.value ?: return
        pendingDeleteId.value = null
        viewModelScope.launch {
            attempt { useCases.debtRepository.deleteDebt(id) }.onFailure { _effect.emit(DebtsEffect.ActionFailed) }
        }
    }
}

private suspend inline fun <T> attempt(block: () -> T): Result<T> =
    runCatching { block() }.onFailure { if (it is CancellationException) throw it }
