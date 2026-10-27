package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

interface NoteRepository {

    /**
     * Emits the notes whose app day lies between [from] and [to], both included, newest first by creation instant,
     * then by id.
     */
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Note>>

    /**
     * Stores a new note with [text] on [appDay], stamped with [createdAt], and returns its id.
     */
    suspend fun add(appDay: LocalDate, createdAt: Instant, text: String): Long

    /**
     * Replaces the text of the note with [id], keeping its app day and creation instant.
     *
     * @throws IllegalStateException when no note has [id].
     */
    suspend fun updateText(id: Long, text: String)

    /**
     * Deletes the note with [id]. A missing id changes nothing.
     */
    suspend fun delete(id: Long)
}
