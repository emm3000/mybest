package com.emm.mybest.features.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.history.GetLogbookUseCase
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.weight.DeleteWeightByDateUseCase
import com.emm.mybest.domain.validation.NoteValidationError
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

private fun initialHistoryState(today: LocalDate): HistoryState {
    val month = YearMonthValue.from(today)
    return HistoryState(
        today = today,
        currentMonth = month,
        selectedMonth = month,
        monthNavigationLabel = month.toNavigationLabel(),
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val getLogbookUseCase: GetLogbookUseCase,
    private val deleteWeightByDate: DeleteWeightByDateUseCase,
    private val deletePhoto: DeletePhotoUseCase,
    private val noteUseCases: HistoryNoteUseCases,
    appDayClock: AppDayClock,
) : ViewModel() {

    private val baseState = initialHistoryState(appDayClock.today())

    private val _selectedMonth = MutableStateFlow(baseState.currentMonth)
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _selectedView = MutableStateFlow(HistoryView.CALENDAR)
    private val _noteEditor = MutableStateFlow<NoteEditorState?>(null)
    private var noteJob: Job? = null

    private val _effect: MutableSharedFlow<HistoryEffect> = effectFlow()
    val effect: SharedFlow<HistoryEffect> = _effect.asSharedFlow()

    val state: StateFlow<HistoryState> = combine(
        _selectedMonth,
        _selectedDate,
    ) { month, selectedDate ->
        Pair(month, selectedDate)
    }.flatMapLatest { (month, _) ->
        combine(
            getHistoryUseCase(month),
            getLogbookUseCase(month),
            _selectedDate,
            _selectedView,
            _noteEditor,
        ) { result, logbook, date, view, noteEditor ->
            baseState.copy(
                selectedMonth = month,
                monthNavigationLabel = month.toNavigationLabel(),
                selectedDate = date,
                monthWeightCount = result.monthWeightCount,
                monthPhotoCount = result.monthPhotoCount,
                monthOutingCount = result.monthOutingCount,
                monthWonNightCount = result.monthWonNightCount,
                dayMarks = result.monthlyData.mapValues { it.value.toMarks() }.toImmutableMap(),
                dayDetail = date?.let { toDayDetailState(it, result.monthlyData[it], baseState.today) },
                selectedView = view,
                logbookDays = logbook.map { it.toState(baseState.today) }.toImmutableList(),
                noteEditor = noteEditor,
                isLoading = false,
                errorMessage = null,
            )
        }
    }.catch { throwable ->
        emit(
            baseState.copy(
                selectedMonth = _selectedMonth.value,
                selectedDate = _selectedDate.value,
                selectedView = _selectedView.value,
                isLoading = false,
                errorMessage = throwable.message ?: "No se pudo cargar el historial.",
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = baseState.copy(isLoading = true),
    )

    fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.OnMonthChange -> _selectedMonth.value = intent.newMonth
            is HistoryIntent.OnDateSelected -> _selectedDate.value = intent.date
            HistoryIntent.OnDateDismiss -> _selectedDate.value = null
            is HistoryIntent.OnViewSelected -> _selectedView.value = intent.view
            is HistoryIntent.NoteIntent -> onNoteIntent(intent)
            is HistoryIntent.OnDeleteWeight -> viewModelScope.launch {
                deleteWeightByDate(intent.date)
            }
            is HistoryIntent.OnDeletePhoto -> viewModelScope.launch {
                deletePhoto(intent.photoId)
            }
        }
    }

    private fun onNoteIntent(intent: HistoryIntent.NoteIntent) {
        when (intent) {
            is HistoryIntent.NoteEditIntent -> onNoteEditIntent(intent)
            is HistoryIntent.NoteDeleteIntent -> onNoteDeleteIntent(intent)
        }
    }

    private fun onNoteEditIntent(intent: HistoryIntent.NoteEditIntent) {
        when (intent) {
            HistoryIntent.OnWriteNoteClick -> _noteEditor.value = NoteEditorState()
            is HistoryIntent.OnNoteClick -> openNote(intent.noteId)
            is HistoryIntent.OnNoteTextChange -> _noteEditor.update { it?.copy(text = intent.text, error = null) }
            HistoryIntent.OnNoteSave -> saveNote()
            HistoryIntent.OnNoteDismiss -> _noteEditor.value = null
        }
    }

    private fun onNoteDeleteIntent(intent: HistoryIntent.NoteDeleteIntent) {
        when (intent) {
            HistoryIntent.OnNoteDeleteClick -> setConfirmingDelete(isConfirming = true)
            HistoryIntent.OnNoteDeleteDismiss -> setConfirmingDelete(isConfirming = false)
            HistoryIntent.OnNoteDeleteConfirm -> deleteNote()
        }
    }

    private fun openNote(noteId: Long) {
        val text = state.value.logbookDays.noteTextOf(noteId) ?: return
        _noteEditor.value = NoteEditorState(noteId = noteId, text = text)
    }

    private fun setConfirmingDelete(isConfirming: Boolean) {
        _noteEditor.update { editor -> editor?.takeIf { it.isEditing }?.copy(isConfirmingDelete = isConfirming) ?: editor }
    }

    private fun saveNote() {
        val editor = _noteEditor.value ?: return
        launchNoteAction {
            attempt { saveEditor(editor) }
                .onSuccess { error -> _noteEditor.update { current -> error?.let { current?.copy(error = it) } } }
                .onFailure { _effect.emit(HistoryEffect.NoteActionFailed) }
        }
    }

    private suspend fun saveEditor(editor: NoteEditorState): NoteValidationError? {
        val noteId = editor.noteId ?: return noteUseCases.add(editor.text)
        return noteUseCases.update(noteId, editor.text)
    }

    private fun deleteNote() {
        val noteId = _noteEditor.value?.noteId ?: return
        launchNoteAction {
            attempt { noteUseCases.delete(noteId) }
                .onSuccess { _noteEditor.value = null }
                .onFailure {
                    setConfirmingDelete(isConfirming = false)
                    _effect.emit(HistoryEffect.NoteActionFailed)
                }
        }
    }

    private fun launchNoteAction(action: suspend () -> Unit) {
        if (noteJob?.isActive == true) return
        noteJob = viewModelScope.launch { action() }
    }
}

private suspend inline fun <T> attempt(block: () -> T): Result<T> =
    runCatching { block() }.onFailure { if (it is CancellationException) throw it }
