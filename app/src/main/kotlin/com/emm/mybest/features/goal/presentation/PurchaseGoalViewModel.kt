package com.emm.mybest.features.goal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.core.money.Money
import com.emm.mybest.core.money.formatPen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PurchaseGoalViewModel(
    private val useCases: PurchaseGoalUseCases,
) : ViewModel() {

    private val goalForm = MutableStateFlow(GoalFormState())
    private val contributionForm = MutableStateFlow<ContributionFormState?>(null)
    private val isClearConfirmationVisible = MutableStateFlow(false)
    private var contributionJob: Job? = null

    private val _effect = effectFlow<PurchaseGoalEffect>()
    val effect: SharedFlow<PurchaseGoalEffect> = _effect.asSharedFlow()

    val state: StateFlow<PurchaseGoalState> = combine(
        useCases.observeGoalProgress(),
        useCases.observeNotSpent(),
        goalForm,
        contributionForm,
        isClearConfirmationVisible,
    ) { progress, notSpent, goalForm, contributionForm, isClearing ->
        PurchaseGoalState(
            isLoaded = true,
            goal = progress?.toSummaryState(),
            notSpentLabel = notSpent.formatPen(),
            goalForm = goalForm,
            contributionForm = contributionForm,
            isClearConfirmationVisible = isClearing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = PurchaseGoalState(),
    )

    fun onIntent(intent: PurchaseGoalIntent) {
        when (intent) {
            is PurchaseGoalIntent.GoalIntent -> onGoalIntent(intent)
            is PurchaseGoalIntent.ContributionIntent -> onContributionIntent(intent)
            is PurchaseGoalIntent.ClearIntent -> onClearIntent(intent)
        }
    }

    private fun onGoalIntent(intent: PurchaseGoalIntent.GoalIntent) {
        when (intent) {
            is PurchaseGoalIntent.OnGoalChange -> goalForm.update { it.withValue(intent.field, intent.value) }
            is PurchaseGoalIntent.OnGoalSave -> saveGoal()
        }
    }

    private fun onContributionIntent(intent: PurchaseGoalIntent.ContributionIntent) {
        when (intent) {
            is PurchaseGoalIntent.OnAddContributionClick -> contributionForm.value = ContributionFormState()
            is PurchaseGoalIntent.OnContributionChange -> contributionForm.value = ContributionFormState(intent.value)
            is PurchaseGoalIntent.OnContributionSave -> saveContribution()
            is PurchaseGoalIntent.OnContributionDismiss -> contributionForm.value = null
            is PurchaseGoalIntent.OnContributionDelete -> deleteContribution(intent.id)
        }
    }

    private fun onClearIntent(intent: PurchaseGoalIntent.ClearIntent) {
        when (intent) {
            is PurchaseGoalIntent.OnClearClick -> isClearConfirmationVisible.value = true
            is PurchaseGoalIntent.OnClearConfirm -> clearGoal()
            is PurchaseGoalIntent.OnClearDismiss -> isClearConfirmationVisible.value = false
        }
    }

    private fun saveGoal() {
        val draft = goalForm.value.toDraft()
        viewModelScope.launch {
            attempt { useCases.saveGoal(draft) }
                .onSuccess { errors ->
                    goalForm.update {
                        if (errors.isEmpty()) {
                            GoalFormState()
                        } else {
                            it.copy(
                                errors = errors.toSet(),
                            )
                        }
                    }
                }
                .onFailure { _effect.emit(PurchaseGoalEffect.ActionFailed) }
        }
    }

    private fun saveContribution() {
        val current = contributionForm.value ?: return
        if (contributionJob?.isActive == true) return
        contributionJob = viewModelScope.launch {
            attempt { useCases.addContribution(Money.parseOrNull(current.amount)) }
                .onSuccess { error -> contributionForm.value = error?.let { current.copy(error = it) } }
                .onFailure { _effect.emit(PurchaseGoalEffect.ActionFailed) }
        }
    }

    private fun deleteContribution(id: Long) {
        viewModelScope.launch {
            attempt { useCases.purchaseGoalRepository.deleteContribution(id) }
                .onFailure { _effect.emit(PurchaseGoalEffect.ActionFailed) }
        }
    }

    private fun clearGoal() {
        isClearConfirmationVisible.value = false
        viewModelScope.launch {
            attempt { useCases.purchaseGoalRepository.clearGoal() }
                .onFailure { _effect.emit(PurchaseGoalEffect.ActionFailed) }
        }
    }
}

private suspend inline fun <T> attempt(block: () -> T): Result<T> =
    runCatching { block() }.onFailure { if (it is CancellationException) throw it }
