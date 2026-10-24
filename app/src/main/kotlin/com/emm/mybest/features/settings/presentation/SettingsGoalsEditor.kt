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

class SettingsGoalsEditor(
    private val observeSetupUseCase: ObserveSetupUseCase,
    private val updateSetupUseCase: UpdateSetupUseCase,
) {
    private val editor = MutableStateFlow<SettingsEditorState?>(null)

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
        val candidate = current.field.withInput(observeSetupUseCase().first(), current.input)
            ?: return showError(SettingsFieldError.INVALID_NUMBER)
        return persist(candidate)
    }

    private suspend fun clear(): SettingsEffect? {
        val current = editor.value ?: return null
        return persist(current.field.cleared(observeSetupUseCase().first()))
    }

    private suspend fun persist(candidate: Setup): SettingsEffect? =
        runCatching { updateSetupUseCase(candidate) }
            .map { errors -> resultOf(errors) }
            .getOrElse { SettingsEffect.GoalSaveFailed }

    private fun resultOf(errors: List<SetupValidationError>): SettingsEffect? {
        val error = errors.firstOrNull() ?: return SettingsEffect.GoalSaved.also { editor.update { null } }
        return showError(error.toFieldError())
    }

    private fun showError(error: SettingsFieldError): SettingsEffect? {
        editor.update { it?.copy(error = error) }
        return null
    }
}
