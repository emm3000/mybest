package com.emm.mybest.features.diet.presentation.edit

data class EditMealSheetCallbacks(
    val onDescriptionChange: (String) -> Unit,
    val onSave: () -> Unit,
    val onCancel: () -> Unit,
)
