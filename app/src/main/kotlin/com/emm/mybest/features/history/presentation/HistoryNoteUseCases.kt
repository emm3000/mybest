package com.emm.mybest.features.history.presentation

import com.emm.mybest.domain.usecase.note.AddNoteUseCase
import com.emm.mybest.domain.usecase.note.DeleteNoteUseCase
import com.emm.mybest.domain.usecase.note.UpdateNoteUseCase

data class HistoryNoteUseCases(
    val add: AddNoteUseCase,
    val update: UpdateNoteUseCase,
    val delete: DeleteNoteUseCase,
)
