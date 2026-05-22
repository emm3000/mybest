package com.emm.mybest.features.exercise.presentation.edit

data class EditExerciseSheetCallbacks(
    val onNameChange: (String) -> Unit,
    val onDetailChange: (String) -> Unit,
    val onVolumeChange: (String) -> Unit,
    val onSave: () -> Unit,
    val onCancel: () -> Unit,
)
