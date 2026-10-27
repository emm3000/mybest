package com.emm.mybest.data

import com.emm.mybest.data.entities.NoteDao
import com.emm.mybest.data.entities.NoteEntity
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.domain.models.Note
import com.emm.mybest.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
) : NoteRepository {

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Note>> =
        noteDao.observeRange(from, to).map { list -> list.map { it.toDomain() } }

    override suspend fun add(appDay: LocalDate, createdAt: Instant, text: String): Long =
        noteDao.insert(NoteEntity(appDay = appDay, createdAtEpochMs = createdAt.toEpochMilliseconds(), text = text))

    override suspend fun updateText(id: Long, text: String) {
        check(noteDao.updateText(id, text) > 0) { "No note with id $id" }
    }

    override suspend fun delete(id: Long) {
        noteDao.delete(id)
    }
}
