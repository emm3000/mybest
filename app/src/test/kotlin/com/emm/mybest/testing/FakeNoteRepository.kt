package com.emm.mybest.testing

import com.emm.mybest.domain.models.Note
import com.emm.mybest.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

class FakeNoteRepository(initialNotes: List<Note> = emptyList()) : NoteRepository {

    private val notes = MutableStateFlow(initialNotes)

    val stored: List<Note> get() = notes.value

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Note>> = notes.map { list ->
        list.filter { it.appDay in from..to }
            .sortedWith(compareByDescending<Note> { it.createdAt }.thenByDescending { it.id })
    }

    override suspend fun add(appDay: LocalDate, createdAt: Instant, text: String): Long {
        val id = (notes.value.maxOfOrNull { it.id } ?: 0L) + 1L
        notes.value = notes.value + Note(id, appDay, createdAt, text)
        return id
    }

    override suspend fun updateText(id: Long, text: String) {
        check(notes.value.any { it.id == id }) { "No note with id $id" }
        notes.value = notes.value.map { if (it.id == id) it.copy(text = text) else it }
    }

    override suspend fun delete(id: Long) {
        notes.value = notes.value.filterNot { it.id == id }
    }
}
