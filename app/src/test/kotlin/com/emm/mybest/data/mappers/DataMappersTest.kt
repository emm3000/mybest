package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.ProgressPhotoEntity
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataMappersTest {

    @Test
    fun `weight entity maps to domain`() {
        val weightEntity = DailyWeightEntity(
            id = "w1",
            date = LocalDate(2026, 3, 8),
            weight = 70.5f,
            photoPath = "/tmp/w.jpg",
            note = "ok",
        )

        val weight = weightEntity.toDomain()

        assertEquals("w1", weight.id)
        assertEquals(70.5f, weight.weight)
        assertEquals("/tmp/w.jpg", weight.photoPath)
    }

    @Test
    fun `photo entity maps to domain`() {
        val photoEntity = ProgressPhotoEntity(
            id = "p1",
            date = LocalDate(2026, 3, 8),
            type = com.emm.mybest.data.entities.PhotoType.FACE,
            photoPath = "/tmp/p.jpg",
            createdAt = 456L,
        )

        val photo = photoEntity.toDomain()

        assertEquals("p1", photo.id)
        assertEquals(PhotoType.FACE, photo.type)
        assertEquals(456L, photo.createdAt)
    }

    @Test
    fun `new progress photo maps to entity`() {
        val input = NewProgressPhoto(
            photoPath = "/tmp/new.jpg",
            type = PhotoType.BODY,
            date = LocalDate(2026, 3, 8),
        )

        val entity = input.toEntity()

        assertEquals(com.emm.mybest.data.entities.PhotoType.BODY, entity.type)
        assertEquals("/tmp/new.jpg", entity.photoPath)
        assertEquals(LocalDate(2026, 3, 8), entity.date)
    }

    @Test
    fun `photo type mapping is symmetrical for all values`() {
        PhotoType.entries.forEach { type ->
            val mapped = type.toData().toDomain()
            assertEquals(type, mapped)
        }

        val hasAllTypes = PhotoType.entries.all {
            com.emm.mybest.data.entities.PhotoType.entries.map { dataType ->
                dataType.toDomain()
            }.contains(it)
        }
        assertTrue(hasAllTypes)
    }
}
