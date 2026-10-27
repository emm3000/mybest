package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.NoteEntity
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteMappersTest {

    @Test
    fun `a note entity round-trips through the domain`() {
        val entity = NoteEntity(
            id = 7,
            appDay = LocalDate(2026, 11, 6),
            createdAtEpochMs = 1_794_022_200_000,
            text = "línea 1\nlínea 2",
        )

        assertEquals(entity, entity.toDomain().toEntity())
    }
}
