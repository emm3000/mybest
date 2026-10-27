package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.NoteEntity
import com.emm.mybest.domain.models.Note
import kotlin.time.Instant

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    appDay = appDay,
    createdAtEpochMs = createdAt.toEpochMilliseconds(),
    text = text,
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    appDay = appDay,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs),
    text = text,
)
