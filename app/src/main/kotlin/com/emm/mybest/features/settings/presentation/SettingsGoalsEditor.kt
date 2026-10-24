package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.usecase.ObserveSetupUseCase
import com.emm.mybest.domain.usecase.UpdateSetupUseCase
import com.emm.mybest.domain.validation.SetupValidationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SettingsGoalsEditor(
    private val observeSetupUseCase: ObserveSetupUseCase,
    private val updateSetupUseCase: UpdateSetupUseCase,
) {
    private val editor = MutableStateFlow<SettingsEditorState?>(null)
    private val saveLock = Mutex()

    val goals: Flow<SettingsGoals> = combine(observeSetupUseCase(), editor, ::SettingsGoals)

    /**
     * Applies [intent] and returns the one-shot effect it caused, if any.
     */
    suspend fun handle(intent: SettingsIntent.FieldIntent): SettingsEffect? = when (intent) {
        is SettingsIntent.OnFieldOpen -> open(intent.field)
        is SettingsIntent.OnFieldInputChange -> changeInput(intent.input)
        is SettingsIntent.OnFieldSave -> save()
        is SettingsIntent.OnFieldClear -> clear()
        is SettingsIntent.OnFieldDismiss -> dismiss()
    }

    private fun changeInput(input: String): SettingsEffect? {
        editor.update { it?.copy(input = input, error = null) }
        return null
    }

    private fun dismiss(): SettingsEffect? {
        editor.update { null }
        return null
    }

    private suspend fun open(field: SettingsField): SettingsEffect? {
        editor.update { SettingsEditorState(field, field.inputFor(observeSetupUseCase().first())) }
        return null
    }

    private suspend fun save(): SettingsEffect? {
        val current = editor.value ?: return null
        return saveLock.withLock {
            val candidate = current.field.withInput(observeSetupUseCase().first(), current.input)
            when (candidate) {
                null -> showError(current.field, SettingsFieldError.INVALID_NUMBER)
                else -> persist(current.field, candidate)
            }
        }
    }

    private suspend fun clear(): SettingsEffect? {
        val current = editor.value ?: return null
        return saveLock.withLock { persist(current.field, current.field.cleared(observeSetupUseCase().first())) }
    }

    private suspend fun persist(field: SettingsField, candidate: Setup): SettingsEffect? =
        runCatching { updateSetupUseCase(candidate) }
            .map { errors -> resultOf(field, errors) }
            .getOrElse { SettingsEffect.GoalSaveFailed }

    private fun resultOf(field: SettingsField, errors: List<SetupValidationError>): SettingsEffect? {
        val error = errors.firstOrNull() ?: return SettingsEffect.GoalSaved.also {
            editor.update { open -> open?.takeUnless { it.field == field } }
        }
        return showError(field, error.toFieldError())
    }

    private fun showError(field: SettingsField, error: SettingsFieldError): SettingsEffect? {
        editor.update { open -> open?.takeIf { it.field == field }?.copy(error = error) ?: open }
        return null
    }
}
