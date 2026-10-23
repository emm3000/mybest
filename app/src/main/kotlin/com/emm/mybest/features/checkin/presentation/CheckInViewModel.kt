package com.emm.mybest.features.checkin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.usecase.checkin.CheckInTask
import com.emm.mybest.domain.usecase.checkin.RecordUnplannedOutingResult
import com.emm.mybest.domain.usecase.checkin.SaveCheckInResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckInViewModel(
    private val useCases: CheckInUseCases,
) : ViewModel() {

    private var isSubmitting = false
    private val form = MutableStateFlow(CheckInForm())
    private val saved = MutableStateFlow<Outing?>(null)
    private val task: StateFlow<CheckInTask?> = useCases.observeTask().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = null,
    )

    private val _effect = effectFlow<CheckInEffect>()
    val effect: SharedFlow<CheckInEffect> = _effect.asSharedFlow()

    val state: StateFlow<CheckInState> = combine(task, form, saved, ::toCheckInState).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = CheckInState(),
    )

    fun onIntent(intent: CheckInIntent) {
        when (intent) {
            CheckInIntent.OnSaveClick -> save()
            CheckInIntent.OnWentOutYesClick -> answerYes()
            CheckInIntent.OnWentOutNoClick -> answerNo()
            CheckInIntent.OnNotNowClick, CheckInIntent.OnDoneClick -> _effect.tryEmit(CheckInEffect.Close)
            else -> form.update { it.reduce(intent) }
        }
    }

    private fun submit(action: suspend () -> Unit) {
        if (isSubmitting) return
        isSubmitting = true
        viewModelScope.launch {
            try {
                action()
            } finally {
                isSubmitting = false
            }
        }
    }

    private fun save() {
        val outing = (task.value as? CheckInTask.PendingOuting)?.outing ?: return
        val draft = form.value.takeIf { saved.value == null && it.isCompleteFor(outing.kind) }?.toDraft() ?: return
        submit {
            attempt { useCases.saveCheckIn(outing, draft) }
                .onSuccess(::onSaveResult)
                .onFailure { _effect.emit(CheckInEffect.ActionFailed) }
        }
    }

    private fun onSaveResult(result: SaveCheckInResult) {
        when (result) {
            is SaveCheckInResult.Saved -> {
                saved.value = result.outing
                form.value = CheckInForm()
            }
            SaveCheckInResult.InvalidMood,
            SaveCheckInResult.MissingSpend,
            SaveCheckInResult.NegativeSpend,
            SaveCheckInResult.MissingDrinks,
            SaveCheckInResult.MissingReturnTime,
            -> _effect.tryEmit(CheckInEffect.InvalidInput)
        }
    }

    private fun answerYes() {
        val question = task.value as? CheckInTask.AskUnplanned ?: return
        submit {
            attempt { useCases.recordUnplannedOuting(question.eveningAppDay) }
                .onSuccess(::onUnplannedResult)
                .onFailure { _effect.emit(CheckInEffect.ActionFailed) }
        }
    }

    private fun onUnplannedResult(result: RecordUnplannedOutingResult) {
        when (result) {
            RecordUnplannedOutingResult.Recorded -> Unit
            RecordUnplannedOutingResult.AlreadyRecorded -> _effect.tryEmit(CheckInEffect.EveningAlreadyRecorded)
        }
    }

    private fun answerNo() {
        val question = task.value as? CheckInTask.AskUnplanned ?: return
        submit {
            attempt { useCases.recordNoOuting(question.eveningAppDay) }
                .onSuccess { _effect.emit(CheckInEffect.Close) }
                .onFailure { _effect.emit(CheckInEffect.ActionFailed) }
        }
    }
}

private suspend inline fun <T> attempt(block: () -> T): Result<T> =
    runCatching { block() }.onFailure { if (it is CancellationException) throw it }
